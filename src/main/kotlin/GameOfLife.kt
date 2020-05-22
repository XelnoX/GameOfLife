package com.game

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class GameOfLife : Application() {
    override fun start(primaryStage: Stage) {
        val root = VBox(10.0)
        val scene = Scene(root, width.toDouble(), (height + 100).toDouble())
        val canvas = Canvas(width.toDouble(), height.toDouble())
        val reset = Button("Reset")
        val random = Button("Random")
        val step = Button("Step")
        val run = Button("Run")
        val stop = Button("Stop")
        root.children.addAll(canvas, HBox(25.0, reset, step, run, stop, random))
        primaryStage.scene = scene
        primaryStage.show()
        val rows = floor(height / cellSize.toDouble()).toInt()
        val cols = floor(width / cellSize.toDouble()).toInt()
        val graphics = canvas.graphicsContext2D
        val life = Life(rows, cols, graphics)
        life.random()
        val runAnimation: AnimationTimer = object : AnimationTimer() {
            private var lastUpdate: Long = 0
            override fun handle(now: Long) {
                // only update once every second
                if (now - lastUpdate >= TimeUnit.MILLISECONDS.toNanos(100)) {
                    life.tick()
                    lastUpdate = now
                }
            }
        }
        reset.onAction = EventHandler { life.reset() }
        run.onAction = EventHandler { runAnimation.start() }
        step.onAction = EventHandler { life.tick() }
        stop.onAction = EventHandler { runAnimation.stop() }
        random.onAction = EventHandler { life.random() }


        scene.onMousePressed = EventHandler { itFirst ->
            life.changeTile(itFirst.x.toInt(), itFirst.y.toInt())
            var oldCol = itFirst.y.toInt() / cellSize
            var oldRow = itFirst.x.toInt() / cellSize
            var newCol: Int
            var newRow: Int
            scene.onMouseDragged = EventHandler { itSecond ->
                newCol = itSecond.y.toInt() / cellSize
                newRow = itSecond.x.toInt() / cellSize
                if(newCol != oldCol || newRow != oldRow){
                    oldCol = newCol
                    oldRow = newRow
                    life.changeTile(itSecond.x.toInt(), itSecond.y.toInt())
                }

            }
        }
    }

    private class Life(private val rows: Int, private val cols: Int, private val graphics: GraphicsContext) {
        private var grid: Array<IntArray>
        private val random = Random()

        fun changeTile(row: Int, col: Int){
            val newRow = (row / cellSize)
            val newCol = (col / cellSize)
            println("$newRow  $newCol")
            if(between(newRow)  && between(newCol)){

                if(grid[newRow][newCol] == 0){
                    grid[newRow][newCol] = 1
                }else{
                    grid[newRow][newCol] = 0
                }
                draw()
            }
        }

        fun between(number: Int): Boolean{
            return number >= 0 && number < grid.size
        }

        fun random() {
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    grid[i][j] = random.nextInt(2)
                }
            }
            draw()
        }

        fun reset() {
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    grid[i][j] = 0
                }
            }
            draw()
        }

        private fun draw() {
            // clear graphics
            graphics.fill = Color.WHITE
            graphics.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())
            for (i in grid.indices) {
                for (j in grid[i].indices) {
                    if (grid[i][j] == 1) {
                        // first rect will end up becoming the border
                        graphics.fill = Color.gray(0.5, 0.5)
                        graphics.fillRect(i * cellSize.toDouble(), j * cellSize.toDouble(), cellSize.toDouble(), cellSize.toDouble())
                        graphics.fill = Color.BLUE
                        graphics.fillRect(i * cellSize + 1.toDouble(), j * cellSize + 1.toDouble(), cellSize - 2.toDouble(), cellSize - 2.toDouble())
                    } else {
                        graphics.fill = Color.gray(0.5, 0.5)
                        graphics.fillRect(i * cellSize.toDouble(), j * cellSize.toDouble(), cellSize.toDouble(), cellSize.toDouble())
                        graphics.fill = Color.WHITE
                        graphics.fillRect(i * cellSize + 1.toDouble(), j * cellSize + 1.toDouble(), cellSize - 2.toDouble(), cellSize - 2.toDouble())
                    }
                }
            }
        }

        fun tick() {
            val next = Array(rows) { IntArray(cols) }
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    val neighbors = countAliveNeighbors(i, j)
                    if (neighbors == 3) {
                        next[i][j] = 1
                    } else if (neighbors < 2 || neighbors > 3) {
                        next[i][j] = 0
                    } else {
                        next[i][j] = grid[i][j]
                    }
                }
            }
            grid = next
            draw()
        }

        private fun countAliveNeighbors(i: Int, j: Int): Int {
            var sum = 0
            val iStart = if (i == 0) 0 else -1
            val iEndInclusive = if (i == grid.size - 1) 0 else 1
            val jStart = if (j == 0) 0 else -1
            val jEndInclusive = if (j == grid[0].size - 1) 0 else 1
            for (k in iStart..iEndInclusive) {
                for (l in jStart..jEndInclusive) {
                    sum += grid[i + k][l + j]
                }
            }
            sum -= grid[i][j]
            return sum
        }

        init {
            grid = Array(rows) { IntArray(cols) }
        }
    }

    companion object {
        private const val width = 750
        private const val height = 750
        private const val cellSize = 15

        @JvmStatic
        fun main(args: Array<String>) {
            launch(GameOfLife::class.java, *args)
        }
    }
}