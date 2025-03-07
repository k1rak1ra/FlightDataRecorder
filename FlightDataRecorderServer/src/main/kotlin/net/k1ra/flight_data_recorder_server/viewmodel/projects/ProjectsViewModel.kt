package net.k1ra.flight_data_recorder_server.viewmodel.projects

import io.ktor.http.Headers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import net.k1ra.flight_data_recorder.model.projects.DatabaseQueryComparison
import net.k1ra.flight_data_recorder.model.projects.ModifyShareRequest
import net.k1ra.flight_data_recorder.model.projects.ProjectData
import net.k1ra.flight_data_recorder.model.projects.QueryDataType
import net.k1ra.flight_data_recorder.model.projects.QueryItem
import net.k1ra.flight_data_recorder.model.projects.QueryRelationship
import net.k1ra.flight_data_recorder.model.projects.UserPermissionLevel
import net.k1ra.flight_data_recorder_server.model.dao.authentication.toSimpleUserData
import net.k1ra.flight_data_recorder_server.model.dao.logging.LogsDao
import net.k1ra.flight_data_recorder_server.model.dao.logging.toLogLine
import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.SharePermissionsDao
import net.k1ra.flight_data_recorder_server.model.dao.projects.toShareData
import net.k1ra.flight_data_recorder_server.utils.Constants
import net.k1ra.flight_data_recorder_server.viewmodel.authentication.UserViewModel
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.extract
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.util.UUID
import kotlin.time.Duration.Companion.minutes


object ProjectsViewModel {
    fun getProject(projectId: String) : ProjectsDao? = transaction {
        ProjectsDao.find { ProjectsDao.ProjectsTable.projectId eq projectId }.limit(1).firstOrNull()
    }

    fun createProject(newName: String, headers: Headers) = transaction {
        val userDao = UserViewModel.userFromHeader(headers)

        if (!ProjectsDao.find { ProjectsDao.ProjectsTable.name eq newName }.empty())
            throw Exception("nameMustBeUnique")

        var newPid = UUID.randomUUID().toString()

        //Make sure UID is unique
        while (!ProjectsDao.find { ProjectsDao.ProjectsTable.projectId eq newPid }.empty())
            newPid = UUID.randomUUID().toString()

        ProjectsDao.new {
            owner = userDao
            projectId = newPid
            name = newName
        }
    }

    fun doesProjectExit(projectId: String, headers: Headers) : Boolean = transaction {
        val userDao = UserViewModel.userFromHeader(headers)

        var project = userDao.ownedProjects.firstOrNull{ it.projectId == projectId }
        if (project == null)
            project = userDao.projectsSharedWithUser.map { it.project }.firstOrNull{ it.projectId == projectId }

        return@transaction project != null
    }

    /**
     * Make me better, this is very basic and naive
     */
    private fun queryToDbQuery(queryStr: String, projectDao: ProjectsDao) : Query {
        if (queryStr.isEmpty())
            return LogsDao.LogsTable.select(LogsDao.LogsTable.columns)
                .where { LogsDao.LogsTable.project eq projectDao.id }
                .orderBy(Pair(LogsDao.LogsTable.datetime, SortOrder.DESC))
                .limit(Constants.MAX_LOG_LINES)

        val parts = queryStr.split(" ")
        val selectIndex = parts.indexOf("SELECT")
        val fromIndex = parts.indexOf("FROM")
        val whereIndex = parts.indexOf("WHERE")

        if (selectIndex != 0)
            throw Exception("syntaxSelect")

        if (fromIndex < 0)
            throw Exception("syntaxNoFrom")

        if (whereIndex in 1..<fromIndex)
            throw Exception("syntaxFromAfterWhere")

        if (whereIndex < 0)
            return LogsDao.LogsTable.select(LogsDao.LogsTable.columns)
                .where { LogsDao.LogsTable.project eq projectDao.id }
                .orderBy(Pair(LogsDao.LogsTable.datetime, SortOrder.DESC))
                .limit(Constants.MAX_LOG_LINES)

        if (parts.size <= whereIndex+1)
            throw Exception("syntaxWhereMissing")

        val whereQuery = parts.subList(whereIndex+1, parts.size).joinToString(" ")

        val tree = processTreeSegment(null, whereQuery).second
        println(tree)
        val queryOp = buildQueryOp(tree)
        println(queryOp)

        return LogsDao.LogsTable.select(LogsDao.LogsTable.columns)
            .where { (LogsDao.LogsTable.project eq projectDao.id) and queryOp }
            .orderBy(Pair(LogsDao.LogsTable.datetime, SortOrder.DESC))
            .limit(Constants.MAX_LOG_LINES)
    }

    private fun buildQueryOp(tree: Pair<QueryRelationship, ArrayList<*>>) : Op<Boolean> {
        if (tree.second.size == 0) {
            throw Exception("syntaxTreeBranchZero")
        } else if (tree.second.size == 1) {
            return if (tree.second[0] is Pair<*, *>) {
                buildQueryOp(tree.second[0] as Pair<QueryRelationship, ArrayList<*>>)
            } else if (tree.second[0] is QueryItem<*>) {
                (tree.second[0] as QueryItem<*>).toOp()
            } else {
                throw Exception("syntaxTreeUnexpectedType")
            }
        } else {
            val opList = tree.second.map {
                if (it is Pair<*, *>) {
                    buildQueryOp(it as Pair<QueryRelationship, ArrayList<*>>)
                } else if (it is QueryItem<*>) {
                    it.toOp()
                } else {
                    throw Exception("syntaxTreeUnexpectedType")
                }
            }

            var op = opList[0]

            when (tree.first) {
                QueryRelationship.AND -> opList.forEach { op = op and it }
                QueryRelationship.OR -> opList.forEach { op = op or it }
            }

            return op
        }
    }

    fun QueryItem<*>.toOp() : Op<Boolean> {
        return when(type) {
            QueryDataType.INT -> when (op) {
                DatabaseQueryComparison.EQUALS -> LogsDao.LogsTable.logLine.extract<Int>(key) eq value as Int
                DatabaseQueryComparison.GREATER -> LogsDao.LogsTable.logLine.extract<Int>(key) greater value as Int
                DatabaseQueryComparison.GREATER_EQ -> LogsDao.LogsTable.logLine.extract<Int>(key) greaterEq value as Int
                DatabaseQueryComparison.LESS -> LogsDao.LogsTable.logLine.extract<Int>(key) less value as Int
                DatabaseQueryComparison.LESS_EQ -> LogsDao.LogsTable.logLine.extract<Int>(key) lessEq value as Int
                DatabaseQueryComparison.LIKE -> throw Exception("syntaxInvalidOp")
            }
            QueryDataType.DOUBLE -> when (op) {
                DatabaseQueryComparison.EQUALS -> LogsDao.LogsTable.logLine.extract<Double>(key) eq value as Double
                DatabaseQueryComparison.GREATER -> LogsDao.LogsTable.logLine.extract<Double>(key) greater value as Double
                DatabaseQueryComparison.GREATER_EQ -> LogsDao.LogsTable.logLine.extract<Double>(key) greaterEq value as Double
                DatabaseQueryComparison.LESS -> LogsDao.LogsTable.logLine.extract<Double>(key) less value as Double
                DatabaseQueryComparison.LESS_EQ -> LogsDao.LogsTable.logLine.extract<Double>(key) lessEq value as Double
                DatabaseQueryComparison.LIKE -> throw Exception("syntaxInvalidOp")
            }
            QueryDataType.BOOL -> when (op) {
                DatabaseQueryComparison.EQUALS -> LogsDao.LogsTable.logLine.extract<Boolean>(key) eq value as Boolean
                else -> throw Exception("syntaxInvalidOp")
            }
            QueryDataType.STRING -> when (op) {
                DatabaseQueryComparison.EQUALS -> LogsDao.LogsTable.logLine.extract<String>(key) eq value as String
                DatabaseQueryComparison.GREATER -> LogsDao.LogsTable.logLine.extract<String>(key) greater value as String
                DatabaseQueryComparison.GREATER_EQ -> LogsDao.LogsTable.logLine.extract<String>(key) greaterEq value as String
                DatabaseQueryComparison.LESS -> LogsDao.LogsTable.logLine.extract<String>(key) less value as String
                DatabaseQueryComparison.LESS_EQ -> LogsDao.LogsTable.logLine.extract<String>(key) lessEq value as String
                DatabaseQueryComparison.LIKE -> LogsDao.LogsTable.logLine.extract<String>(key) like value as String
            }
        }
    }

    private fun processTreeSegment(endChar: Char?, query: String) : Pair<Int, Pair<QueryRelationship, ArrayList<*>>> {
        var escaping = false

        val currentResult = Pair(QueryRelationship.OR, arrayListOf(Pair(QueryRelationship.AND, arrayListOf<Any>())))
        var onIndex = 0

        val currentQuery = StringBuilder()
        var consumedCharacters = 0
        var charactersToSkip = 0
        val matchingList = arrayListOf<Char>()

        query.forEachIndexed { i, it ->
            consumedCharacters++

            if (charactersToSkip > 0) {
                charactersToSkip--
            } else {
                if (escaping) {
                    escaping = false
                    currentQuery.append(it)
                } else {
                    when (it) {
                        '(' -> {
                            if (matchingList.isEmpty()) {
                                if (currentQuery.toString().isNotBlank())
                                    throw Exception("syntaxBracketsError")

                                val result = processTreeSegment(')', query.substring(i+1, query.length))
                                charactersToSkip = result.first
                                currentResult.second[onIndex].second.add(result.second)
                            } else {
                                currentQuery.append(it)
                            }
                        }
                        '"' -> {
                            currentQuery.append(it)

                            if (matchingList.lastOrNull() == '"')
                                matchingList.removeLast()
                            else
                                matchingList.add('"')
                        }
                        '\'' -> {
                            currentQuery.append(it)

                            if (matchingList.lastOrNull() == '\'')
                                matchingList.removeLast()
                            else
                                matchingList.add('\'')
                        }
                        '`' -> {
                            currentQuery.append(it)

                            if (matchingList.lastOrNull() == '`')
                                matchingList.removeLast()
                            else
                                matchingList.add('`')
                        }
                        endChar -> {
                            if (currentQuery.toString().isNotBlank())
                                currentResult.second[onIndex].second.add(consumeQuery(currentQuery.toString()))

                            return Pair(consumedCharacters, currentResult)
                        }
                        '\\' -> {
                            escaping = true
                        }
                        else -> currentQuery.append(it)
                    }
                }

                if (matchingList.isEmpty() && currentQuery.endsWith("AND")) {
                    val query = currentQuery.toString().substring(0, currentQuery.toString().length - 3)
                    if (query.isNotBlank())
                        currentResult.second[onIndex].second.add(consumeQuery(query))
                    currentQuery.clear()
                }

                if (matchingList.isEmpty() && currentQuery.endsWith("OR")) {
                    val query = currentQuery.toString().substring(0, currentQuery.toString().length - 2)
                    if (query.isNotBlank())
                        currentResult.second[onIndex].second.add(consumeQuery(query))
                    currentQuery.clear()

                    currentResult.second.add(Pair(QueryRelationship.AND, arrayListOf()))
                    onIndex++
                }
            }
        }

        if (currentQuery.toString().isNotBlank())
            currentResult.second[onIndex].second.add(consumeQuery(currentQuery.toString()))

        return Pair(consumedCharacters, currentResult)
    }

    private fun consumeQuery(query: String) : QueryItem<*> {
        val trimmed = query.trim()

        val quotationType = trimmed.last()

        if (quotationType == '"' || quotationType == '\'' || quotationType == '`')  {
            val firstInstanceOfQuotation = trimmed.indexOfFirst { it == quotationType }
            val queryValue = trimmed.substring(firstInstanceOfQuotation+1, trimmed.length-1)
            val nameAndOp = trimmed.split(quotationType).first().replace(" ","")

            val key: String

            val operation = if (nameAndOp.endsWith("=")) {
                key = nameAndOp.substring(0, nameAndOp.length-1)
                DatabaseQueryComparison.EQUALS
            } else if (nameAndOp.endsWith(">")) {
                key = nameAndOp.substring(0, nameAndOp.length-1)
                DatabaseQueryComparison.GREATER
            } else if (nameAndOp.endsWith(">=")) {
                key = nameAndOp.substring(0, nameAndOp.length-2)
                DatabaseQueryComparison.GREATER_EQ
            } else if (nameAndOp.endsWith("<")) {
                key = nameAndOp.substring(0, nameAndOp.length-1)
                DatabaseQueryComparison.LESS
            } else if (nameAndOp.endsWith("<=")) {
                key = nameAndOp.substring(0, nameAndOp.length-2)
                DatabaseQueryComparison.LESS_EQ
            } else if (nameAndOp.lowercase().endsWith("like")) {
                key = nameAndOp.substring(0, nameAndOp.length-4)
                DatabaseQueryComparison.LIKE
            } else {
                throw Exception("syntaxInvalidCompareForString")
            }

            return QueryItem(key, operation, queryValue, QueryDataType.STRING)
        } else if (trimmed.endsWith("false") || trimmed.endsWith("true")) {
            val queryValue = trimmed.endsWith("true")
            val nameAndOp = trimmed.substring(0, trimmed.length-5).replace(" ","")

            val key: String

            val operation = if (nameAndOp.endsWith("=")) {
                key = nameAndOp.substring(0, nameAndOp.length-1)
                DatabaseQueryComparison.EQUALS
            } else {
                throw Exception("syntaxInvalidCompareForBool")
            }

            return QueryItem(key, operation, queryValue, QueryDataType.BOOL)
        } else {
            if (trimmed.contains(">")) {
                val split = trimmed.replace(" ","").split(">")

                val key = split.first()
                val value = split.last()

                return if (value.contains("."))
                    QueryItem(key, DatabaseQueryComparison.GREATER, value.toDouble(), QueryDataType.DOUBLE)
                else
                    QueryItem(key, DatabaseQueryComparison.GREATER, value.toInt(), QueryDataType.INT)

            } else if (trimmed.contains(">=")) {
                val split = trimmed.replace(" ","").split(">=")

                val key = split.first()
                val value = split.last()

                return if (value.contains("."))
                    QueryItem(key, DatabaseQueryComparison.GREATER_EQ, value.toDouble(), QueryDataType.DOUBLE)
                else
                    QueryItem(key, DatabaseQueryComparison.GREATER_EQ, value.toInt(), QueryDataType.INT)

            } else if (trimmed.contains("<")) {
                val split = trimmed.replace(" ","").split("<")

                val key = split.first()
                val value = split.last()

                return if (value.contains("."))
                    QueryItem(key, DatabaseQueryComparison.LESS, value.toDouble(), QueryDataType.DOUBLE)
                else
                    QueryItem(key, DatabaseQueryComparison.LESS, value.toInt(), QueryDataType.INT)

            } else if (trimmed.contains("<=")) {
                val split = trimmed.replace(" ","").split("<=")

                val key = split.first()
                val value = split.last()

                return if (value.contains("."))
                    QueryItem(key, DatabaseQueryComparison.LESS_EQ, value.toDouble(), QueryDataType.DOUBLE)
                else
                    QueryItem(key, DatabaseQueryComparison.LESS_EQ, value.toInt(), QueryDataType.INT)

            } else if (trimmed.contains("=")) {
                val split = trimmed.replace(" ","").split("=")

                val key = split.first()
                val value = split.last()

                return if (value.contains("."))
                    QueryItem(key, DatabaseQueryComparison.EQUALS, value.toDouble(), QueryDataType.DOUBLE)
                else
                    QueryItem(key, DatabaseQueryComparison.EQUALS, value.toInt(), QueryDataType.INT)

            } else {
                throw Exception("syntaxInvalidCompareForNumber")
            }
        }
    }

    fun getProjectData(projectId: String, headers: Headers, query: String) : ProjectData? = transaction {
        val userDao = UserViewModel.userFromHeader(headers)

        var project = userDao.ownedProjects.firstOrNull{ it.projectId == projectId }
        if (project == null)
            project = userDao.projectsSharedWithUser.map { it.project }.firstOrNull{ it.projectId == projectId }
        if (project == null)
            return@transaction null

        val fiveMinutesAgo = Clock.systemUTC().instant().toKotlinInstant().minus(5.minutes).toLocalDateTime(TimeZone.UTC)

        val selectedLogLines = LogsDao.wrapRows(queryToDbQuery(query, project)).toList().map { it.toLogLine() }

        val currentLiveIps = LogsDao
            .find { (LogsDao.LogsTable.project eq project.id) and (LogsDao.LogsTable.datetime greaterEq fiveMinutesAgo) }
            .distinctBy { it.ipAddr }
            .map { IpGeolocationViewModel.get(it.ipAddr) }

        val permissionLevel = if (userDao.ownedProjects.contains(project))
            UserPermissionLevel.OWNER
        else
            userDao.projectsSharedWithUser.first { it.project.projectId == projectId }.permissionLevel

        return@transaction ProjectData(
            currentLiveIps,
            selectedLogLines,
            project.name,
            projectId,
            permissionLevel,
            project.owner.toSimpleUserData(),
            project.shares.map { it.toShareData() }
        )
    }

    fun updateOwner(newOwner: String, headers: Headers, projectId: String) = transaction {
        val userDao = UserViewModel.userFromHeader(headers)
        val newOwnerDao = UserViewModel.fetchByUidIfExists(newOwner)!!

        val projectDao = userDao.ownedProjects.firstOrNull{ it.projectId == projectId }
        if (projectDao == null)
            throw Exception("insufficientPermissions")

        projectDao.shares.find { it.user.id.value == newOwnerDao.id.value }?.delete()
        projectDao.owner = newOwnerDao
        projectDao.flush()

        SharePermissionsDao.new {
            user = userDao
            project = projectDao
            permissionLevel = UserPermissionLevel.WRITE
        }
    }

    fun deleteShare(deleteShare: String, headers: Headers, projectId: String) = transaction {
        val userDao = UserViewModel.userFromHeader(headers)

        var project = userDao.ownedProjects.firstOrNull{ it.projectId == projectId }
        if (project == null)
            project = userDao.projectsSharedWithUser.filter { it.permissionLevel == UserPermissionLevel.WRITE }.map { it.project }.firstOrNull{ it.projectId == projectId }
        if (project == null)
            throw Exception("insufficientPermissions")

        project.shares.find { it.user.uid == deleteShare }?.delete()
    }

    fun addShare(addShare: String, headers: Headers, projectId: String) = transaction {
        val userDao = UserViewModel.userFromHeader(headers)
        val newUserDao = UserViewModel.fetchByUidIfExists(addShare)!!

        var projectDao = userDao.ownedProjects.firstOrNull{ it.projectId == projectId }
        if (projectDao == null)
            projectDao = userDao.projectsSharedWithUser.filter { it.permissionLevel == UserPermissionLevel.WRITE }.map { it.project }.firstOrNull{ it.projectId == projectId }
        if (projectDao == null)
            throw Exception("insufficientPermissions")

        projectDao.shares.find { it.user.id.value == newUserDao.id.value }?.delete()
        SharePermissionsDao.new {
            user = newUserDao
            project = projectDao
            permissionLevel = UserPermissionLevel.READONLY
        }
    }

    fun editShare(editShare: ModifyShareRequest, headers: Headers, projectId: String) = transaction {
        val userDao = UserViewModel.userFromHeader(headers)

        var projectDao = userDao.ownedProjects.firstOrNull{ it.projectId == projectId }
        if (projectDao == null)
            projectDao = userDao.projectsSharedWithUser.filter { it.permissionLevel == UserPermissionLevel.WRITE }.map { it.project }.firstOrNull{ it.projectId == projectId }
        if (projectDao == null)
            throw Exception("insufficientPermissions")

        projectDao.shares.find { it.user.uid == editShare.uid }?.permissionLevel = editShare.level
    }
}