package com.guoshengkai.litechatgpt.core.util.hibernate.jdbc.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author gsk( from hiberbate source)
 */
@SuppressWarnings("unchecked")
public class BasicFormatterImpl implements Formatter {

    private static final Set BEGIN_CLAUSES = new HashSet();
    private static final Set END_CLAUSES = new HashSet();
    private static final Set LOGICAL = new HashSet();
    private static final Set QUANTIFIERS = new HashSet();
    private static final Set DML = new HashSet();
    private static final Set MISC = new HashSet();
    static final String indentString = "    ";
    static final String initial = "\n    ";

    static {
        BEGIN_CLAUSES.add("left");
        BEGIN_CLAUSES.add("right");
        BEGIN_CLAUSES.add("inner");
        BEGIN_CLAUSES.add("outer");
        BEGIN_CLAUSES.add("group");
        BEGIN_CLAUSES.add("order");
        END_CLAUSES.add("where");
        END_CLAUSES.add("set");
        END_CLAUSES.add("having");
        END_CLAUSES.add("join");
        END_CLAUSES.add("from");
        END_CLAUSES.add("by");
        END_CLAUSES.add("join");
        END_CLAUSES.add("into");
        END_CLAUSES.add("union");
        LOGICAL.add("and");
        LOGICAL.add("or");
        LOGICAL.add("when");
        LOGICAL.add("else");
        LOGICAL.add("end");
        QUANTIFIERS.add("in");
        QUANTIFIERS.add("all");
        QUANTIFIERS.add("exists");
        QUANTIFIERS.add("some");
        QUANTIFIERS.add("any");
        DML.add("insert");
        DML.add("update");
        DML.add("delete");
        MISC.add("select");
        MISC.add("on");
    }

    @Override
    public String format(String source) {
        return new FormatProcess(source).perform();
    }

    private static class FormatProcess {
        boolean beginLine = true;
        boolean afterBeginBeforeEnd = false;
        boolean afterByOrSetOrFromOrSelect = false;
        boolean afterOn = false;
        boolean afterBetween = false;
        boolean afterInsert = false;
        int inFunction = 0;
        int parensSinceSelect = 0;
        private LinkedList parenCounts = new LinkedList();
        private LinkedList afterByOrFromOrSelects = new LinkedList();
        int indent = 1;
        StringBuffer result = new StringBuffer();
        StringTokenizer tokens;
        String lastToken;
        String token;
        String lcToken;

        public FormatProcess(String sql) {
            this.tokens = new StringTokenizer(
                    sql,
                    "()+*/-=<>'`\"[], \n\r\f\t",
                    true);

        }

        public String perform() {
            this.result.append("\n    ");
            while (this.tokens.hasMoreTokens()) {
                this.token = this.tokens.nextToken();
                this.lcToken = this.token.toLowerCase();
                if ("'".equals(this.token)) {
                    do {
                        String t = this.tokens.nextToken();
                        this.token += t;
                        if ("'".equals(t)) {
                            break;
                        }
                    }
                    while (
                            this.tokens.hasMoreTokens());

                } else if ("\"".equals(this.token)) {
                    String t;
                    do {
                        t = this.tokens.nextToken();
                        this.token += t;
                    }
                    while (!
                            "\"".equals(t));

                }
                if ((this.afterByOrSetOrFromOrSelect) && (",".equals(this.token))) {
                    commaAfterByOrFromOrSelect();
                } else if ((this.afterOn) && (",".equals(this.token))) {
                    commaAfterOn();
                } else if ("(".equals(this.token)) {
                    openParen();
                } else if (")".equals(this.token)) {
                    closeParen();
                } else if (BasicFormatterImpl.BEGIN_CLAUSES.contains(this.lcToken)) {
                    beginNewClause();
                } else if (BasicFormatterImpl.END_CLAUSES.contains(this.lcToken)) {
                    endNewClause();
                } else if ("select".equals(this.lcToken)) {
                    select();
                } else if (BasicFormatterImpl.DML.contains(this.lcToken)) {
                    updateOrInsertOrDelete();
                } else if ("values".equals(this.lcToken)) {
                    values();
                } else if ("on".equals(this.lcToken)) {
                    on();
                } else if ((this.afterBetween) && (this.lcToken.equals("and"))) {
                    misc();
                    this.afterBetween = false;
                } else if (BasicFormatterImpl.LOGICAL.contains(this.lcToken)) {
                    logical();
                } else if (isWhitespace(this.token)) {
                    white();
                } else {
                    misc();
                }
                if (!isWhitespace(this.token)) {
                    this.lastToken = this.lcToken;
                }
            }
            return this.result.toString();
        }

        private void commaAfterOn() {
            out();
            this.indent -= 1;
            newline();
            this.afterOn = false;
            this.afterByOrSetOrFromOrSelect = true;
        }

        private void commaAfterByOrFromOrSelect() {
            out();
            newline();
        }

        private void logical() {
            if ("end".equals(this.lcToken)) {
                this.indent -= 1;
            }
            newline();
            out();
            this.beginLine = false;
        }

        private void on() {
            this.indent += 1;
            this.afterOn = true;
            newline();
            out();
            this.beginLine = false;
        }

        private void misc() {
            out();
            if ("between".equals(this.lcToken)) {
                this.afterBetween = true;
            }
            if (this.afterInsert) {
                newline();
                this.afterInsert = false;
            } else {
                this.beginLine = false;
                if ("case".equals(this.lcToken)) {
                    this.indent += 1;
                }

            }
        }

        private void white() {
            if (!this.beginLine) {
                this.result.append(" ");
            }
        }

        private void updateOrInsertOrDelete() {
            out();
            this.indent += 1;
            this.beginLine = false;
            if ("update".equals(this.lcToken)) {
                newline();
            }
            if ("insert".equals(this.lcToken)) {
                this.afterInsert = true;
            }
        }

        private void select() {
            out();
            this.indent += 1;
            newline();
            this.parenCounts.addLast(new Integer(this.parensSinceSelect));
            this.afterByOrFromOrSelects.addLast(Boolean.valueOf(this.afterByOrSetOrFromOrSelect));
            this.parensSinceSelect = 0;
            this.afterByOrSetOrFromOrSelect = true;
        }

        private void out() {
            this.result.append(this.token);
        }

        private void endNewClause() {
            if (!this.afterBeginBeforeEnd) {
                this.indent -= 1;
                if (this.afterOn) {
                    this.indent -= 1;
                    this.afterOn = false;
                }
                newline();
            }
            out();
            if (!"union".equals(this.lcToken)) {
                this.indent += 1;
            }
            newline();
            this.afterBeginBeforeEnd = false;
            this.afterByOrSetOrFromOrSelect = (("by".equals(this.lcToken)) ||
                    ("set".equals(this.lcToken)) ||
                    ("from".equals(this.lcToken)));
        }


        private void beginNewClause() {
            if (!this.afterBeginBeforeEnd) {
                if (this.afterOn) {
                    this.indent -= 1;
                    this.afterOn = false;

                }
                this.indent -= 1;
                newline();

            }
            out();
            this.beginLine = false;
            this.afterBeginBeforeEnd = true;

        }


        private void values() {
            this.indent -= 1;
            newline();
            out();
            this.indent += 1;
            newline();
        }


        private void closeParen() {
            this.parensSinceSelect -= 1;
            if (this.parensSinceSelect < 0) {
                this.indent -= 1;
                this.parensSinceSelect = ((Integer) this.parenCounts.removeLast()).intValue();
                this.afterByOrSetOrFromOrSelect = ((Boolean) this.afterByOrFromOrSelects.removeLast()).booleanValue();

            }
            if (this.inFunction > 0) {
                this.inFunction -= 1;
                out();

            } else {
                if (!this.afterByOrSetOrFromOrSelect) {
                    this.indent -= 1;
                    newline();
                }
                out();

            }
            this.beginLine = false;

        }


        private void openParen() {
            if ((isFunctionName(this.lastToken)) || (this.inFunction > 0)) {
                this.inFunction += 1;

            }
            this.beginLine = false;
            if (this.inFunction > 0) {
                out();

            } else {
                out();
                if (!this.afterByOrSetOrFromOrSelect) {
                    this.indent += 1;
                    newline();
                    this.beginLine = true;
                }

            }
            this.parensSinceSelect += 1;

        }


        private static boolean isFunctionName(String tok) {
            char begin = tok.charAt(0);
            boolean isIdentifier = (Character.isJavaIdentifierStart(begin)) || ('"' == begin);

            return (isIdentifier) &&
                    (!BasicFormatterImpl.LOGICAL.contains(tok)) &&
                    (!BasicFormatterImpl.END_CLAUSES.contains(tok)) &&
                    (!BasicFormatterImpl.QUANTIFIERS.contains(tok)) &&
                    (!BasicFormatterImpl.DML.contains(tok)) &&
                    (!BasicFormatterImpl.MISC.contains(tok));

        }


        private static boolean isWhitespace(String token) {
            return " \n\r\f\t".indexOf(token) >= 0;

        }


        private void newline() {
            this.result.append("\n");
            for (int i = 0; i < this.indent; i++) {
                this.result.append("    ");

            }
            this.beginLine = true;

        }

    }

}
