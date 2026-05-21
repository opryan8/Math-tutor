package com.example.data

import kotlin.random.Random

data class Lesson(
    val id: String,
    val title: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val summary: String,
    val content: String,
    val quizCategory: String,
    val xpReward: Int = 50
)

data class MathProblem(
    val equation: String,
    val solution: String,
    val steps: List<String>,
    val options: List<String>,
    val category: String
)

object MathEngine {

    val lessonsList = listOf(
        Lesson(
            id = "linear_intro",
            title = "Introduction to Linear Equations",
            difficulty = "Beginner",
            summary = "Learn how to solve single-variable algebraic equations using inverse operations.",
            content = """
                An **Equation** is like a balance scale. Whatever operations you do to one side, you MUST do to the other side to keep it balanced.
                
                ### Inverse Operations
                To isolate the variable (`x`), we perform the opposite operation of whatever is currently happening to it:
                - Opposite of **Addition** (`+`) is **Subtraction** (`-`)
                - Opposite of **Subtraction** (`-`) is **Addition** (`+`)
                - Opposite of **Multiplication** (`\times`) is **Division** (`\div`)
                - Opposite of **Division** (`\div`) is **Multiplication** (`\times`)

                ### Example:
                Solve: `x + 7 = 19`
                - Subtract 7 from BOTH sides:
                  `x + 7 - 7 = 19 - 7`
                  `x = 12`
                  
                ### Two-Step Linear Equations:
                Solve: `3x - 5 = 16`
                - **Step 1:** Undo addition/subtraction. Add 5 to both sides:
                  `3x = 21`
                - **Step 2:** Undo multiplication/division. Divide both sides by 3:
                  `x = 7`
            """.trimIndent(),
            quizCategory = "Linear Equations",
            xpReward = 50
        ),
        Lesson(
            id = "inequalities",
            title = "Understanding Algebraic Inequalities",
            difficulty = "Beginner",
            summary = "Master inequality symbols and learn the golden rule when multiplying or dividing by negatives.",
            content = """
                An **Inequality** compares two algebraic values using symbols:
                - `<` (Less than)
                - `>` (Greater than)
                - `\le` (Less than or equal to)
                - `\ge` (Greater than or equal to)

                ### Solving Inequalities
                You solve inequalities exactly like equations, with one **CRITICAL GOLDEN RULE**:
                
                > ⚠️ **The Golden Rule:** When you multiply or divide both sides of an inequality by a **NEGATIVE NUMBER**, you must **FLIP** the direction of the inequality symbol!

                ### Example:
                Solve: `-2x < 10`
                - Divide both sides by `-2`:
                  `x > -5` (Notice how `<` flipped to `>`)
                  
                ### Why does this happen?
                Consider `3 < 5`. If we multiply both sides by `-1`, we get `-3` and `-5`. Since `-3` is larger than `-5`, we must write `-3 > -5`. Flipping the sign keeps the statement true!
            """.trimIndent(),
            quizCategory = "Inequalities",
            xpReward = 50
        ),
        Lesson(
            id = "exponent_laws",
            title = "The Rules of Exponents",
            difficulty = "Intermediate",
            summary = "Simplify exponents easily using Product, Quotient, and Power Rules.",
            content = """
                Exponents represent repeated multiplication of a base: `x^3 = x \times x \times x`.
                
                Here are the core rules to simplify algebraic terms with exponents:

                ### 1. Product Rule
                Multiplying same bases? **Add** the exponents!
                - `x^a \cdot x^b = x^{a + b}`
                - Example: `x^3 \cdot x^4 = x^{3 + 4} = x^7`

                ### 2. Quotient Rule
                Dividing same bases? **Subtract** the exponents!
                - `x^a \div x^b = x^{a - b}`
                - Example: `x^8 \div x^3 = x^{8 - 3} = x^5`

                ### 3. Power of a Power Rule
                Raising a power to another power? **Multiply** them!
                - `(x^a)^b = x^{a \cdot b}`
                - Example: `(x^2)^5 = x^{2 \times 5} = x^{10}`

                ### 4. Zero Exponent Rule
                Any non-zero base raised to power of 0 is always 1!
                - `x^0 = 1`
            """.trimIndent(),
            quizCategory = "Exponents",
            xpReward = 50
        ),
        Lesson(
            id = "quadratic_factoring",
            title = "Factoring Quadratic Trinomials",
            difficulty = "Advanced",
            summary = "Learn to decompose quadratic equations of type `x^2 + bx + c = 0` into binomials.",
            content = """
                Quadratic expression are expressions of degree 2: `x^2 + bx + c`.
                
                ### Factoring Strategy: "Product and Sum"
                To factor `x^2 + bx + c` into binomials of type `(x + p)(x + q)`:
                1. Find two numbers, `p` and `q`, that:
                   - **Multiply** to give `c` (the product)
                   - **Add** to give `b` (the sum)

                ### Example:
                Factor: `x^2 + 5x + 6`
                - We need numbers that multiply to `6` and add to `5`.
                - Let's check factors of `6`:
                  - `1 \times 6 = 6` (Add to: `7`)
                  - `2 \times 3 = 6` (Add to: `5`) - **Perfect match!**
                - Write the factors:
                  `(x + 2)(x + 3)`
                  
                ### Solving by Factoring:
                Set `(x + 2)(x + 3) = 0`.
                - To equal `0`, either `x + 2 = 0` or `x + 3 = 0`.
                - So, the roots are `x = -2` or `x = -3`!
            """.trimIndent(),
            quizCategory = "Quadratic Factoring",
            xpReward = 60
        )
    )

    fun generateProblem(category: String): MathProblem {
        val random = Random(System.nanoTime())
        return when (category) {
            "Inequalities" -> {
                // Generate: ax < b or ax > b
                // We pick coefficient 'a' and solution 'x', then compute 'b'
                val isNegative = random.nextBoolean()
                val a = if (isNegative) random.nextInt(-6, -1) else random.nextInt(2, 7)
                val x = random.nextInt(-10, 11).let { if (it == 0) 5 else it }
                val b = a * x
                val sign = listOf("<", ">", "<=", ">=").random(random)
                
                // Solve step explanation
                val steps = mutableListOf<String>()
                steps.add("Standard inequality: ${a}x $sign $b")
                
                val finalSign = if (a < 0) {
                    val flipped = when(sign) {
                        "<" -> ">"
                        ">" -> "<"
                        "<=" -> ">="
                        ">=" -> "<="
                        else -> sign
                    }
                    steps.add("Divide both sides by negative coefficient '$a'. Remember to FLIP the inequality sign!")
                    flipped
                } else {
                    steps.add("Divide both sides by positive coefficient '$a'. The inequality direction stays the same.")
                    sign
                }
                steps.add("Calculated: x $finalSign ($b / $a)")
                steps.add("Final solution: x $finalSign $x")

                val correctAns = "x $finalSign $x"
                val wrongAns1 = "x $sign $x"
                val wrongAns2 = "x $finalSign ${x + random.nextInt(1, 4)}"
                val wrongAns3 = "x ${if (finalSign.startsWith("<")) ">" else "<"} ${x - random.nextInt(1, 4)}"

                val options = listOf(correctAns, wrongAns1, wrongAns2, wrongAns3).distinct().shuffled()
                MathProblem(
                    equation = "Solve the inequality: ${a}x $sign $b",
                    solution = correctAns,
                    steps = steps,
                    options = if (options.size < 4) listOf(correctAns, "x $finalSign 2", "x $finalSign 6", "x $finalSign -1") else options,
                    category = "Inequalities"
                )
            }
            "Exponents" -> {
                val rulesList = listOf("product", "quotient", "power")
                when (rulesList.random(random)) {
                    "product" -> {
                        val a = random.nextInt(2, 10)
                        val b = random.nextInt(2, 10)
                        val eq = "x^$a \\cdot x^$b"
                        val ans = "x^${a + b}"
                        val steps = listOf(
                            "According to the Exponent Product Rule: x^a \\cdot x^b = x^{a + b}",
                            "Add the exponents: $a + $b",
                            "Result: x^${a + b}"
                        )
                        val options = listOf(ans, "x^${a * b}", "x^${Math.max(a, b) - Math.min(a, b)}", "x^${a + b + 2}").distinct().shuffled()
                        MathProblem("Simplify the expression: $eq", ans, steps, options, "Exponents")
                    }
                    "quotient" -> {
                        val a = random.nextInt(6, 15)
                        val b = random.nextInt(2, 5)
                        val eq = "x^$a / x^$b"
                        val ans = "x^${a - b}"
                        val steps = listOf(
                            "According to the Exponent Quotient Rule: x^a / x^b = x^{a - b}",
                            "Subtract the exponents: $a - $b",
                            "Result: x^${a - b}"
                        )
                        val options = listOf(ans, "x^${a / b}", "x^${a + b}", "x^${a * b}").distinct().shuffled()
                        MathProblem("Simplify the expression: $eq", ans, steps, options, "Exponents")
                    }
                    else -> { // power
                        val a = random.nextInt(2, 6)
                        val b = random.nextInt(2, 6)
                        val eq = "(x^$a)^$b"
                        val ans = "x^${a * b}"
                        val steps = listOf(
                            "According to the Exponent Power Rule: (x^a)^b = x^{a \\cdot b}",
                            "Multiply the inner and outer exponents: $a \\cdot $b",
                            "Result: x^${a * b}"
                        )
                        val options = listOf(ans, "x^${a + b}", "x^${a - b}", "x^${a * b + 2}").distinct().shuffled()
                        MathProblem("Simplify the expression: $eq", ans, steps, options, "Exponents")
                    }
                }
            }
            "Quadratic Factoring" -> {
                // Generate (x - r1)(x - r2) = x^2 + bx + c
                // Let roots be pretty integers -5 to 5 (not 0)
                val r1 = listOf(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5).random(random)
                val r2 = listOf(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5).random(random)
                
                // p and q to represent binomial constants:
                // (x - r1)(x - r2) => p = -r1, q = -r2
                val p = -r1
                val q = -r2
                
                val b = p + q // coefficient of x
                val c = p * q // constant
                
                val eqSignB = if (b >= 0) "+ ${b}" else "- ${Math.abs(b)}"
                val eqSignC = if (c >= 0) "+ ${c}" else "- ${Math.abs(c)}"
                
                val equationStr = "x^2 ${if (b == 0) "" else eqSignB + "x"} $eqSignC = 0"
                
                val signP = if (p >= 0) "+ $p" else "- ${Math.abs(p)}"
                val signQ = if (q >= 0) "+ $q" else "- ${Math.abs(q)}"
                val ans = "(x $signP)(x $signQ)"
                
                val steps = listOf(
                    "We need to find two binomial factors of type (x + p)(x + q) = 0",
                    "The components must: Multiply to give constant c ($c) and Add to give x-coefficient b ($b)",
                    "Test values that solve this relation: $p and $q",
                    "Check: ($p) * ($q) = $c, and ($p) + ($q) = $b. It works!",
                    "Rewrite in factored form: (x $signP)(x $signQ) = 0"
                )

                val altP1 = if (p >= 0) "- $p" else "+ ${Math.abs(p)}"
                val altQ1 = if (q >= 0) "- $q" else "+ ${Math.abs(q)}"
                val wrong1 = "(x $altP1)(x $altQ1)"
                val wrong2 = "(x $signP)(x $altQ1)"
                val wrong3 = "(x $signQ)(x $altP1)"

                val options = listOf(ans, wrong1, wrong2, wrong3).distinct().shuffled()
                MathProblem(
                    equation = "Factor completely: $equationStr",
                    solution = ans,
                    steps = steps,
                    options = if (options.size < 4) listOf(ans, "(x + 1)(x - 1)", "(x + 2)(x + 3)", "(x - 5)(x + 5)") else options,
                    category = "Quadratic Factoring"
                )
            }
            else -> { // "Linear Equations" (two-step equations ax + b = c)
                val a = random.nextInt(2, 10)
                val x = random.nextInt(1, 15)
                val b = random.nextInt(-15, 16).let { if (it == 0) -4 else it }
                val c = a * x + b
                
                val equationStr = "$a x ${if (b > 0) "+ $b" else "- ${Math.abs(b)}"} = $c"
                val ans = "$x"
                
                val steps = listOf(
                    "Equation: $equationStr",
                    "Isolate the x term. Since we have ${if (b > 0) "+ $b" else "- ${Math.abs(b)}"}, perform reverse operation by ${if (b > 0) "subtracting $b" else "adding ${Math.abs(b)}"} on both sides:",
                    "Result: $a x = ${c - b}",
                    "Isolate x. Divide both sides by the coefficient '$a':",
                    "Solution: x = ${c - b} / $a",
                    "Simplified: x = $x"
                )

                val wrong1 = "${x + random.nextInt(1, 4)}"
                val wrong2 = "${x - random.nextInt(1, 3)}"
                val wrong3 = "${x * 2}"

                val options = listOf(ans, wrong1, wrong2, wrong3).distinct().shuffled()
                MathProblem(
                    equation = "Solve for x: $equationStr",
                    solution = ans,
                    steps = steps,
                    options = if (options.size < 4) listOf(ans, "2", "6", "8") else options,
                    category = "Linear Equations"
                )
            }
        }
    }
}
