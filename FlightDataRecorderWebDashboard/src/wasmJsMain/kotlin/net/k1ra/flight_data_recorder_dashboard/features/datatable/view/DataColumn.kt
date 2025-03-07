/**
 * From: https://github.com/sproctor/compose-data-table
 */

package net.k1ra.flight_data_recorder_dashboard.features.datatable.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

data class DataColumn(
    val alignment: Alignment = Alignment.CenterStart,
    val width: TableColumnWidth = TableColumnWidth.Flex(1f),
    val onSort: ((columnIndex: Int, ascending: Boolean) -> Unit)? = null,
    val isSortIconTrailing: Boolean = true,
    val header: @Composable () -> Unit,
)