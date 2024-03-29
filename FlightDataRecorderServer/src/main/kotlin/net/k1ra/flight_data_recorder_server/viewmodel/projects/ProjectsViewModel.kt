package net.k1ra.flight_data_recorder_server.viewmodel.projects

import net.k1ra.flight_data_recorder_server.model.dao.projects.ProjectsDao
import org.jetbrains.exposed.sql.transactions.transaction


object ProjectsViewModel {
    fun getProject(appKey: String) : ProjectsDao? = transaction {
        ProjectsDao.find { ProjectsDao.ProjectsTable.appKey eq appKey }.limit(1).firstOrNull()
    }
}