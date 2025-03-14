package net.k1ra.flight_data_recorder_dashboard.features.project.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonPrimitive
import net.k1ra.flight_data_recorder.model.projects.ProjectData
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultTextField
import net.k1ra.flight_data_recorder_dashboard.features.datatable.view.DataColumn
import net.k1ra.flight_data_recorder_dashboard.features.datatable.view.DataTable
import net.k1ra.flight_data_recorder_dashboard.features.project.viewmodel.ProjectViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.formatDateTime
import net.k1ra.flight_data_recorder_dashboard.utils.secondaryFontSize
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProjectLogQueryView(viewModel: ProjectViewModel, data: ProjectData) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val query = remember { mutableStateOf(viewModel.query.value) }
    val scrollState = rememberScrollState()

    Column(
        Modifier.fillMaxSize().verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DefaultTextField(query, stringResource(Res.string.log_query), keyboardController, modifier = Modifier.fillMaxWidth().padding(16.dp), suffix = {
            IconButton(onClick = {
                viewModel.query.value = query.value
                viewModel.getData()
            }){
                Icon(Icons.Filled.Search, "search")
            }
        })

        if (data.queriedLogLines.isNotEmpty()) {
            val queryList = viewModel.query.value
                .split("FROM")
                .first()
                .replace("SELECT ","")
                .replace(" ","")
                .split(",")

            println(queryList)

            if (queryList.first() == "count(*)") {
                Text(stringResource(Res.string.entries, data.queriedLogLines.size), fontSize = secondaryFontSize)
            } else {
                val listSize = data.queriedLogLines.size

                val subList = if (listSize > 200)
                    data.queriedLogLines.subList(0, 200)
                else
                    data.queriedLogLines

                val keys = subList.first().contents.keys.toMutableList()

                subList.forEach { logLine ->
                    logLine.contents.keys.forEach { key ->
                        if (!keys.contains(key))
                            keys.add(key)
                    }
                }

                val filteredKeys = if (queryList.first() == "*")
                    keys
                else
                    keys.filter { queryList.contains(it) }

                DataTable(
                    columns = filteredKeys.map {
                        DataColumn(alignment = Alignment.Center) {
                            Text(
                                it,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(2.dp, 0.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    subList.map { logLine ->
                        row {
                            filteredKeys.map { key ->
                                cell {
                                    var content =
                                        (logLine.contents[key] as JsonPrimitive?)?.content ?: ""
                                    if (key == "DateTime")
                                        content = Instant.fromEpochMilliseconds(content.toLong())
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .formatDateTime()

                                    content.replace("\n", "")
                                    content.replace("\r", "")
                                    val maxLength = if (content.length > 200)
                                        200
                                    else
                                        content.length

                                    SelectionContainer {
                                        Text(content.substring(0, maxLength))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(stringResource(Res.string.no_entries_found))
        }
    }
}