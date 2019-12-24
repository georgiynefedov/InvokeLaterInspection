package com.intellij.codeInspection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.StringTokenizer;


public class InvokeLaterInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.InvokeLaterInspection");

    // Defines the text of the quick fix intention
    public static final String QUICK_FIX_NAME = "OK";

    // This string holds a list of classes relevant to this inspection.
    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public String CHECKED_CLASSES = "java.lang.String;java.util.Date";

    /**
     * This method is called to get the panel describing the inspection.
     * It is called every time the user selects the inspection in preferences.
     * The user has the option to edit the list of CHECKED_CLASSES.
     * Adds a document listener to see if
     *
     * @return panel to display inspection information.
     */
    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedClasses = new JTextField(CHECKED_CLASSES);
        checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                CHECKED_CLASSES = checkedClasses.getText();
            }
        });
        panel.add(checkedClasses);
        return panel;
    }

    /**
     * This method is overridden to provide a custom visitor
     *
     * @param holder     object for visitor to register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return non-null visitor for this inspection.
     * @see JavaElementVisitor
     */
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            /**
             *  This string defines the short message shown to a user signaling the inspection
             *  found a problem. It reuses a string from the inspections bundle.
             */
            @NonNls
            private final String DESCRIPTION_TEMPLATE = "May produce NullPointerException #loc";


            /**
             * Evaluate psi method call expressions to see if they are of type
             * SwingUtilities.invokeLater($runnable$)
             *
             * @param expression  The method call expression to be evaluated.
             */
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiReferenceExpression PsiRE = expression.getMethodExpression();
                if (PsiRE.getQualifiedName().equals("SwingUtilities.invokeLater"))
                    holder.registerProblem(expression, DESCRIPTION_TEMPLATE);

            }


            /**
             * Verifies the input is the correct {@code PsiType} for this inspection.
             *
             * @param type  The {@code PsiType} to be examined for a match
             * @return      {@code true} if input is {@code PsiClassType} and matches
             *                 one of the classes in the CHECKED_CLASSES list.
             */
            private boolean isCheckedType(PsiType type) {
                if (!(type instanceof PsiClassType)) return false;
                StringTokenizer tokenizer = new StringTokenizer(CHECKED_CLASSES, ";");
                while (tokenizer.hasMoreTokens()) {
                    String className = tokenizer.nextToken();
                    if (type.equalsToText(className)) return true;
                }
                return false;
            }

        };
    }

}