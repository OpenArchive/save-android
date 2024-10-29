package net.opendasharchive.openarchive.lint

import com.android.tools.lint.client.api.IssueRegistry

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*

class RepositoryUsageDetector : Detector(), Detector.UastScanner {
    companion object {
        val ISSUE = Issue.create(
            id = "DirectRepositoryAccess",
            briefDescription = "Direct Repository access in UI components",
            explanation = """
                Accessing Repositories directly from Fragments or Activities violates clean architecture principles.
                Use a ViewModel to mediate between UI components and Repositories.
                """,
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                RepositoryUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private val UI_COMPONENTS = listOf(
            "android.app.Activity",
            "androidx.activity.ComponentActivity",
            "androidx.fragment.app.Fragment"
        )
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                if (!isUIComponent(node, context)) return

                // Check all declarations in the class
                node.uastDeclarations.forEach { declaration ->
                    when (declaration) {
                        is UVariable -> {
                            val type = declaration.type
                            val typeClass = context.evaluator.getTypeClass(type)
                            if (typeClass?.qualifiedName?.endsWith("Repository") == true) {
                                context.report(
                                    ISSUE,
                                    context.getLocation(declaration as UElement),
                                    "Avoid using Repositories directly in UI components. Use a ViewModel instead."
                                )
                            }
                        }
                    }
                }
            }

            private fun isUIComponent(node: UClass, context: JavaContext): Boolean {
                return UI_COMPONENTS.any { baseClass ->
                    context.evaluator.inheritsFrom(node, baseClass, true)
                }
            }
        }
    }
}

class RepositoryUsageRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(RepositoryUsageDetector.ISSUE)

    override val api: Int = CURRENT_API

    override val minApi: Int = 8
}