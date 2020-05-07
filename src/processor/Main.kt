package processor

import java.util.*
import kotlin.math.pow
import kotlin.system.exitProcess

val scanner = Scanner(System.`in`)

interface Matrix {
    fun add(other: Matrix): Matrix
    fun multiplyToAConstant(k: Double): Matrix
    fun multiply(other: Matrix): Matrix
    fun determinant(): Double
    fun inverse(): Matrix
    fun transposeAlongMainDiagonal(): Matrix
    fun transposeAlongSideDiagonal(): Matrix
    fun transposeVertical(): Matrix
    fun transposeHorizontal(): Matrix
    fun matrix(): Array<Array<Double>>
}

open class MatrixImpl(private var matrix: Array<Array<Double>>): Matrix {

    private val n: Int = matrix.size  // rows
    private val m: Int = matrix[0].size // columns

    override fun add(other: Matrix): Matrix {
        val second = other.matrix()

        if (n != second.size && m != second[0].size) {
            println("ERROR")
            exitProcess(0)
        }

        val sum = Array(n) { Array(m) { 0.0 } }

        for (i in 0 until n) {
            for (j in 0 until m) {
                sum[i][j] = this.matrix[i][j] + second[i][j]
            }
        }

        return MatrixImpl(sum)
    }

    override fun multiplyToAConstant(k: Double): Matrix {
        val prod = Array(n) { Array(m) { 0.0 } }
        for (i in 0 until n) {
            for (j in 0 until m) {
                prod[i][j] = this.matrix[i][j] * k
            }
        }
        return MatrixImpl(prod)
    }

    override fun multiply(other: Matrix): Matrix {
        val second = other.matrix()
        val prod = Array(n) { Array(second[0].size) { 0.0 } }
        if (m != second.size) {
            println("ERROR")
            exitProcess(0)
        }

        for (i in 0 until n) {
            for (j in second[0].indices) {
                var sum = 0.0
                for (k in 0 until m) {
                    sum += this.matrix[i][k] * second[k][j]
                }
                prod[i][j] = sum
            }
        }

        return MatrixImpl(prod)
    }

    override fun determinant(): Double {
        return calculateDeterminant(this.matrix)
    }

    private fun calculateDeterminant(matrix: Array<Array<Double>>): Double {
        if (matrix.size == 2 && matrix[0].size == 2) {
            return ((matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]))
        }

        var determinant = 0.0
        for (j in matrix[0].indices) {
            determinant += matrix[0][j] * (-1.0).pow(j + 2) * calculateDeterminant(subMatrix(j, matrix))
        }
        return determinant
    }

    private fun subMatrix(column: Int, matrix: Array<Array<Double>>): Array<Array<Double>> {
        // initializing matrix with size one less row and column
        val matrix2D = Array(matrix.size - 1) { Array(matrix[0].size - 1) { 0.0 } }


        // copy all elements till the column which should be ignored. Then skip the
        // column that has to ignored and copy all the elements from all rows
        // since doing first row expansion always, the row pointer starts from 1 (0 being first
        // row is ignored).

        var i = 1
        var j = 0
        while (i < matrix.size) {
            while (j < matrix[0].size && j < column) {
                matrix2D[i - 1][j] = matrix[i][j]
                j++
            }
            j = 0
            i++
        }
        j = column + 1
        i = 1
        while (i < matrix.size) {
            while (j < matrix[0].size) {
                matrix2D[i - 1][j - 1] = matrix[i][j]
                j++
            }
            j = column + 1
            i++
        }

        return matrix2D
    }

    // FORMULA: (1/det(Matrix A) * Adjoint(A)
    // Adjoint(A) - Transpose of Matrix of all cofactors of A
    override fun inverse(): Matrix {
        return adjoint().multiplyToAConstant(1/determinant())
    }

    override fun transposeAlongMainDiagonal(): Matrix {

        val transformMatrix = Array(m) { Array(n) { 0.0 } }

        for (i in 0 until n) {
            for (j in 0 until m) {
                transformMatrix[j][i] = this.matrix[i][j]
            }
        }
        return MatrixImpl(transformMatrix)
    }

    override fun transposeAlongSideDiagonal(): Matrix {
        val transformMatrix = this.matrix.clone()
        for (i in 0 until n) {
            for (j in 0 until n - i) {
                var temp = transformMatrix[i][j]
                transformMatrix[i][j] = transformMatrix[n - 1 - j][n - 1 - i]
                transformMatrix[n - 1 - j][n - 1 - i] = temp
            }
        }
        return MatrixImpl(transformMatrix)

    }

    override fun transposeVertical(): Matrix {
        val transformMatrix = this.matrix.clone()

        for (i in 0 until n) {
            var k = 0
            var j = m - 1
            while (k < j) {
                var temp = transformMatrix[i][k]
                transformMatrix[i][k] = transformMatrix[i][j]
                transformMatrix[i][j] = temp
                k++
                j--
            }

        }

        return MatrixImpl(transformMatrix)
    }

    override fun transposeHorizontal(): Matrix {
        val transformMatrix = this.matrix.clone()

        for (j in 0 until m) {
            var i = 0
            var k = n - 1

            while (i < k) {
                var temp = transformMatrix[i][j]
                transformMatrix[i][j] = transformMatrix[k][j]
                transformMatrix[k][j] = temp
                i++
                k--
            }

        }
        return MatrixImpl(transformMatrix)
    }

    override fun matrix(): Array<Array<Double>> {
        return this.matrix
    }

    private fun adjoint(): Matrix {
        val matrix2D = Array(n){ Array(m) {0.0} }

        for (i in 0 until n) {
            for (j in 0 until m) {
                matrix2D[i][j] = cofactor(i, j)
            }
        }
        val matrix = MatrixImpl(matrix2D)
        return matrix.transposeAlongMainDiagonal()
    }

    private fun cofactor(row: Int, column: Int): Double {
        val matrix2D = Array(n-1){Array(m-1){0.0} }
        val bag = LinkedList<Double>()

        for (i in 0 until n){
            for (j in 0 until m) {
                if (i == row || j == column) {
                    continue
                }
                bag.add(this.matrix[i][j])
            }
        }

        for (i in matrix2D.indices){
            for (j in matrix2D[0].indices) {
                matrix2D[i][j] = bag.pollFirst()
            }
        }

        return calculateDeterminant(matrix2D) * (-1.0).pow((row + column + 2).toDouble())
    }
}



fun main() {
    val scanner = Scanner(System.`in`)
    do {
        println("1. Add matrices\n2. Multiply matrix to a constant\n3. Multiply matrices\n" +
                "4. Transpose Matrix\n5. Calculate a determinant\n6. Inverse Matrix\n0. Exit")
        println("Enter your choice:")
        val ch = scanner.nextInt()
        when (ch) {
            1 -> add()
            2 -> multiplyToAConstant()
            3 -> multiply()
            4 -> transposeMatrix()
            5 -> calcDeterminant()
            6 -> inverseOfMatrix()
            0 -> exitProcess(0)
        }
    } while (ch < 4)
}

fun inverseOfMatrix() {
    val matrix2D = initMatrix()
    val matrix = MatrixImpl(matrix2D)

    println("The result is:")
    printMatrix(matrix.inverse())

}

fun calcDeterminant() {

    val matrix2D = initMatrix()
    val matrix = MatrixImpl(matrix2D)

    println("The result is:")
    println(matrix.determinant())
}

fun transposeMatrix() {
    println("\n1. Main diagonal\n2. Side diagonal\n3. Vertical line\n4. Horizontal line")
    println("\nYour choice:")
    when (scanner.nextInt()) {
        1 -> {
            val matrix2D = initMatrix()
            val matrixTranspose: Matrix = MatrixImpl(matrix2D).transposeAlongMainDiagonal()
            printMatrix(matrixTranspose)
        }
        2 -> {
            val matrix2D = initMatrix()
            val matrixTranspose: Matrix = MatrixImpl(matrix2D).transposeAlongSideDiagonal()
            printMatrix(matrixTranspose)
        }
        3 -> {
            val matrix2D = initMatrix()
            val matrixTranspose: Matrix = MatrixImpl(matrix2D).transposeVertical()
            printMatrix(matrixTranspose)
        }
        4 -> {
            val matrix2D = initMatrix()
            val matrixTranspose: Matrix = MatrixImpl(matrix2D).transposeHorizontal()
            printMatrix(matrixTranspose)
        }
    }
}

fun initMatrix(): Array<Array<Double>> {
    println("Enter Matrix Size: ")
    val n = scanner.nextInt()
    val m = scanner.nextInt()
    println("Enter Matrix:")
    val matrix2D = Array(n) { Array(m) { 0.0 } }
    for (i in 0 until n) {
        for (j in 0 until m) {
            matrix2D[i][j] = scanner.nextDouble()
        }
    }

    return matrix2D
}


fun add() {

    println("Enter size of first matrix")
    val nA = scanner.nextInt()
    val mA = scanner.nextInt()
    println("Enter first matrix")
    val A = Array(nA) { Array(mA) { 0.0 } }
    for (i in 0 until nA) {
        for (j in 0 until mA) {
            A[i][j] = scanner.nextDouble()
        }
    }

    val matrixA: Matrix = MatrixImpl(A)

    println("Enter size of second matrix")
    val nB = scanner.nextInt()
    val mB = scanner.nextInt()
    val B = Array(nB) { Array(mB) { 0.0 } }
    println("Enter second matrix")
    for (i in 0 until nB) {
        for (j in 0 until mB) {
            B[i][j] = scanner.nextDouble()
        }
    }

    val matrixB: Matrix = MatrixImpl(B)
    val matrixSum: Matrix = matrixA.add(matrixB)
    println("The result of addition is:")
    printMatrix(matrixSum)

}

fun multiplyToAConstant() {

    println("Enter size of first matrix")
    val nA = scanner.nextInt()
    val mA = scanner.nextInt()

    val A = Array(nA) { Array(mA) { 0.0 } }
    println("Enter first matrix")
    for (i in 0 until nA) {
        for (j in 0 until mA) {
            A[i][j] = scanner.nextDouble()
        }
    }
    val matrix: Matrix = MatrixImpl(A)

    println("Enter a constant")
    val k = scanner.nextDouble()
    println("The multiplication result is:")
    val prodMatrix: Matrix = matrix.multiplyToAConstant(k)
    printMatrix(prodMatrix)
}

fun multiply() {
    println("Enter size of first matrix")
    val nA = scanner.nextInt()
    val mA = scanner.nextInt()

    val A = Array(nA) { Array(mA) { 0.0 } }
    println("Enter first matrix")
    for (i in 0 until nA) {
        for (j in 0 until mA) {
            A[i][j] = scanner.nextDouble()
        }
    }

    val matrixA: Matrix = MatrixImpl(A)

    println("Enter size of second matrix")
    val nB = scanner.nextInt()
    val mB = scanner.nextInt()

    val B = Array(nB) { Array(mB) { 0.0 } }

    println("Enter second matrix")
    for (i in 0 until nB) {
        for (j in 0 until mB) {
            B[i][j] = scanner.nextDouble()
        }
    }

    val matrixB: Matrix = MatrixImpl(B)
    val prodMatrix = matrixA.multiply(matrixB)

    println("The multiplication result is:")
    printMatrix(prodMatrix)
}

fun printMatrix(matrix: Matrix) {
    for (i in matrix.matrix().indices) {
        for (j in 0 until matrix.matrix()[0].size) {
            print("${matrix.matrix()[i][j]} ")
        }
        println()
    }
}
