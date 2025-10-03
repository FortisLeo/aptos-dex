package com.rishitgoklani.aptosdex.presentation.tokendetail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.tradingview.lightweightcharts.api.chart.models.color.IntColor
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.series.models.BarData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.view.ChartsView
import com.rishitgoklani.aptosdex.presentation.tokendetail.CandlestickData

/**
 * TradingView lightweight chart component
 * Displays candlestick chart with app's theme colors
 */
@Composable
fun TradingViewChart(
    chartData: List<CandlestickData>,
    modifier: Modifier = Modifier
) {
    // Store series reference in composable state
    var seriesRef by remember { mutableStateOf<Any?>(null) }

    // Get the app's surface color for seamless integration
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = remember(surfaceColor) {
        android.graphics.Color.argb(
            (surfaceColor.alpha * 255).toInt(),
            (surfaceColor.red * 255).toInt(),
            (surfaceColor.green * 255).toInt(),
            (surfaceColor.blue * 255).toInt()
        )
    }

    AndroidView(
        factory = { context ->
            ChartsView(context).apply {
                // Configure chart with app's background color
                api.applyOptions {
                    layout = com.tradingview.lightweightcharts.api.options.models.layoutOptions {
                        background = SolidColor(backgroundColor)
                        textColor = IntColor("#B0B0B0".toColorInt())
                    }
                    grid = com.tradingview.lightweightcharts.api.options.models.gridOptions {
                        vertLines = com.tradingview.lightweightcharts.api.options.models.gridLineOptions {
                            visible = false
                        }
                        horzLines = com.tradingview.lightweightcharts.api.options.models.gridLineOptions {
                            visible = false
                        }
                    }
                    rightPriceScale = com.tradingview.lightweightcharts.api.options.models.priceScaleOptions {
                        borderVisible = false
                    }
                    timeScale = com.tradingview.lightweightcharts.api.options.models.timeScaleOptions {
                        borderVisible = false
                        visible = true
                    }
                }

                // Add candlestick series and store reference
                api.addCandlestickSeries(
                    onSeriesCreated = { series ->
                        seriesRef = series
                        // Set initial data
                        val barDataList = chartData.map { candle ->
                            BarData(
                                time = Time.Utc(candle.time),
                                open = candle.open,
                                high = candle.high,
                                low = candle.low,
                                close = candle.close
                            )
                        }
                        series.setData(barDataList)
                        api.timeScale.fitContent()
                    }
                )
            }
        },
        update = { view ->
            // Update chart data when chartData changes
            android.util.Log.d("TradingViewChart", "Update called with ${chartData.size} candles")

            if (seriesRef == null) {
                android.util.Log.e("TradingViewChart", "Series reference is null!")
                return@AndroidView
            }

            val barDataList = chartData.map { candle ->
                BarData(
                    time = Time.Utc(candle.time),
                    open = candle.open,
                    high = candle.high,
                    low = candle.low,
                    close = candle.close
                )
            }

            // Update data using the series reference
            seriesRef?.let { series ->
                android.util.Log.d("TradingViewChart", "Series class: ${series.javaClass.name}")
                try {
                    val setDataMethod = series.javaClass.getMethod("setData", List::class.java)
                    android.util.Log.d("TradingViewChart", "Found setData method, invoking with ${barDataList.size} items")
                    setDataMethod.invoke(series, barDataList)
                    view.api.timeScale.fitContent()
                    android.util.Log.d("TradingViewChart", "Chart updated successfully")
                } catch (e: NoSuchMethodException) {
                    android.util.Log.e("TradingViewChart", "setData method not found. Available methods: ${series.javaClass.methods.joinToString { it.name }}", e)
                } catch (e: Exception) {
                    android.util.Log.e("TradingViewChart", "Error updating chart", e)
                }
            }
        },
        modifier = modifier
    )
}
