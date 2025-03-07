/**
 * From: https://github.com/sproctor/compose-data-table
 */

package net.k1ra.flight_data_recorder_dashboard.features.datatable.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout model that arranges its children into rows and columns.
 */
@Composable
fun DataTable(
    columns: List<DataColumn>,
    modifier: Modifier = Modifier,
    state: DataTableState = rememberDataTableState(),
    separator: @Composable () -> Unit = { HorizontalDivider() },
    headerHeight: Dp = 56.dp,
    rowHeight: Dp = 52.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    headerBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    footerBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    rowBackgroundColor: @Composable (Int) -> Color = { MaterialTheme.colorScheme.surface },
    footer: @Composable () -> Unit = { },
    sortColumnIndex: Int? = null,
    sortAscending: Boolean = true,
    logger: ((String) -> Unit)? = null,
    content: DataTableScope.() -> Unit
) {
    BasicDataTable(
        columns = columns,
        modifier = modifier,
        state = state,
        separator = separator,
        headerHeight = headerHeight,
        rowHeight = rowHeight,
        contentPadding = contentPadding,
        headerBackgroundColor = headerBackgroundColor,
        footerBackgroundColor = footerBackgroundColor,
        rowBackgroundColor = rowBackgroundColor,
        footer = footer,
        cellContentProvider = Material3CellContentProvider,
        sortColumnIndex = sortColumnIndex,
        sortAscending = sortAscending,
        logger = logger,
        content = content
    )
}