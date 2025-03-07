/**
 * From: https://github.com/sproctor/compose-data-table
 */

package net.k1ra.flight_data_recorder_dashboard.features.datatable.view

/**
 * Collects measurements for the children of a column that
 * are available to implementations of [TableColumnWidth].
 */
class TableMeasurable internal constructor(
    /**
     * Computes the preferred width of the child by measuring with infinite constraints.
     */
    val preferredWidth: () -> Int,
    /**
     * Computes the minimum intrinsic width of the child for the given available height.
     */
    val minIntrinsicWidth: (Int) -> Int,
    /**
     * Computes the maximum intrinsic width of the child for the given available height.
     */
    val maxIntrinsicWidth: (Int) -> Int
)