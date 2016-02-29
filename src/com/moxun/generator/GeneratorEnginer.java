package com.moxun.generator;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Generating code
 * Created by moxun on 15/12/9.
 */
public class GeneratorEnginer {
    private Map<String, PsiClass> dataSet = new HashMap<String, PsiClass>();
    private Project project;
    private PsiDirectory directory;
    private PsiElementFactory factory;
    private String pkgName;
    private String[] inters;

    public GeneratorEnginer(Project proj, PsiDirectory dir) {
        dataSet.clear();
        project = proj;
        directory = dir;
        factory = JavaPsiFacade.getInstance(project).getElementFactory();
    }

    public void init(String pkg, String[] its) {
        pkgName = pkg;
        if (its != null) {
            inters = its.clone();
        }
    }

    public void append(final String s, final String clsName) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                new WriteCommandAction(project) {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        PsiClass dist = dataSet.get(clsName);
                        if (s.startsWith("//")) {
                            // TODO: issues here.
                            //PsiComment comment = factory.createCommentFromText(s, null);
                            //dist.add(comment);
                            Logger.error("Class " + dist.getQualifiedName() + " has no fields, you need checking it maybe.");
                        } else {
                            String r = s.replaceAll("-", "_");
                            PsiField field = factory.createFieldFromText(r, dist);
                            dist.add(field);
                        }
                    }
                }.execute();
            }
        });
    }

    public void preGen(String name, String last) {
        final PsiFile psiFile = directory.findFile(name + ".java");
        String clazzName = name;
        if (psiFile != null) {
            clazzName = last + name;
            Logger.warn("File [" + name + ".java" + "] already exists, assign [" + clazzName + "] to current class.");
            if (directory.findFile(clazzName + ".java") != null) {
                for (int i = 1; i <= 50; i++) {
                    if (directory.findFile(clazzName + i + ".java") == null) {
                        Logger.warn("File [" + clazzName + ".java" + "] already exists, assign [" + clazzName + i + "] to current class.");
                        clazzName = clazzName + i;
                        break;
                    }
                }
            }
        }

        final PsiClass clazz = JavaDirectoryService.getInstance().createClass(directory, clazzName, "moxun_generator_common");
        dataSet.put(name, clazz);
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        if (inters != null) {
            for (String inter : inters) {
                PsiClass[] psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(inter, searchScope);
                if (psiClasses.length > 0) {
                    final PsiJavaCodeReferenceElement ref = factory.createClassReferenceElement(psiClasses[0]);
                    final PsiMethod[] methods = psiClasses[0].getAllMethods();
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new WriteCommandAction(project) {
                                @Override
                                protected void run(@NotNull Result result) throws Throwable {
                                    clazz.getImplementsList().add(ref);
                                    ((PsiJavaFile) clazz.getContainingFile()).setPackageName(pkgName);
                                    for (PsiMethod m : methods) {
                                        if (m.getModifierList().hasModifierProperty("abstract")) {
                                            PsiMethod psiMethod = null;
                                            try {
                                                psiMethod = factory.createMethod(m.getName(), m.getReturnType());
                                                for (PsiElement param : m.getParameterList().getParameters()) {
                                                    psiMethod.getParameterList().add(param);
                                                }
                                                if (getReturnStatement(m.getReturnType()) != null) {
                                                    PsiStatement statement = factory.createStatementFromText("return " + getReturnStatement(m.getReturnType()) + ";\n", psiMethod);
                                                    psiMethod.getBody().add(statement);
                                                }
                                                psiMethod.getModifierList().addAnnotation("Override");
                                            } catch (NullPointerException npe) {
                                                //do nothing
                                                Logger.error("NPE: " + npe.toString());
                                            }
                                            if (psiMethod != null) {
                                                clazz.add(psiMethod);
                                            }
                                        }
                                    }
                                }
                            }.execute();
                        }
                    });
                } else {
                    Logger.warn("Java interface [" + inter + "] not found in this project.");
                }
            }
        }
    }

    private String getReturnStatement(PsiType type) {
        if (type.equalsToText("void")) {
            return null;
        } else if (type.equalsToText("boolean")) {
            return "false";
        } else if (type.equalsToText("short") || type.equalsToText("byte") || type.equalsToText("int")
                || type.equalsToText("long") || type.equalsToText("float") || type.equalsToText("double")) {
            return "0";
        } else {
            return "null";
        }
    }
}
