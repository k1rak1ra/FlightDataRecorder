/**
 * From: https://github.com/sproctor/compose-data-table
 */

package net.k1ra.flight_data_recorder_dashboard.features.datatable.view

import androidx.compose.runtime.Composable

interface CellContentProvider {
    @Composable
    fun RowCellContent(content: @Composable () -> Unit)

    @Composable
    fun HeaderCellContent(
        sorted: Boolean,
        sortAscending: Boolean,
        isSortIconTrailing: Boolean,
        onClick: (() -> Unit)?,
        content: @Composable () -> Unit
    )
}

object DefaultCellContentProvider : CellContentProvider {

    @Composable
    override fun RowCellContent(content: @Composable () -> Unit) {
        content()
    }

    @Composable
    override fun HeaderCellContent(
        sorted: Boolean,
        sortAscending: Boolean,
        isSortIconTrailing: Boolean,
        onClick: (() -> Unit)?,
        content: @Composable () -> Unit
    ) {
        content()
    }
}