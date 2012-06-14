package hudson.plugins.analysis.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * {@link TreeString} is an alternative string representation
 * that saves the memory when you have a large number of strings
 * that share common prefixes (such as various file names.)
 *
 * <p>
 * {@link TreeString} can be built with {@link TreeStringBuilder}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class TreeString {
    /**
     * Parent node that represents the prefix.
     */
    private TreeString parent;

    /**
     * {@link #parent}+{@link #label} is the string value of this node.
     */
    private char[] label;

    /**
     * Creates a new root {@link TreeString}
     */
    /*package*/ TreeString() {
        this(null,"");
    }

    /*package*/ TreeString(TreeString parent, String label) {
        assert parent==null || label.length()>0; // if there's a parent, label can't be empty.

        this.parent = parent;
        this.label = label.toCharArray(); // string created as a substring of another string can have a lot of garbage attached to it.
    }

    /*package*/ String getLabel() {
        return new String(label);
    }

    /**
     * Inserts a new node between this node and its parent,
     * and returns the newly inserted node.
     *
     * <p>
     * This operation doesn't change the string representation of this node.
     */
    /*package*/ TreeString split(String prefix) {
        assert getLabel().startsWith(prefix);
        char[] suffix = new char[label.length-prefix.length()];
        System.arraycopy(label,prefix.length(),suffix,0,suffix.length);

        TreeString middle = new TreeString(parent,prefix);
        label = suffix;
        parent = middle;

        return middle;
    }

    /**
     * How many nodes do we have from the root to this node (including 'this' itself?)
     *
     * Thus depth of the root node is 1.
     */
    private int depth() {
        int i = 0;
        for (TreeString p=this; p!=null; p=p.parent)
            i++;
        return i;
    }

    public boolean equals(Object rhs) {
        return rhs.getClass()==TreeString.class && ((TreeString)rhs).getLabel().equals(getLabel());
    }

    public int hashCode() {
        int h = parent == null ? 0 : parent.hashCode();

        for (int i = 0; i < label.length; i++)
            h = 31 * h + label[i];

        assert toString().hashCode()==h;
        return h;
    }

    /**
     * Returns the full string representation.
     */
    public String toString() {
        char[][] tokens = new char[depth()][];
        int i=tokens.length;
        int sz=0;
        for (TreeString p=this; p!=null; p=p.parent) {
            tokens[--i] = p.label;
            sz += p.label.length;
        }

        StringBuilder buf = new StringBuilder(sz);
        for (char[] token : tokens)
            buf.append(token);

        return buf.toString();
    }

    /**
     * Interns {@link #label}
     */
    /*package*/ void dedup(Map<String, char[]> table) {
        String l = getLabel();
        char[] v = table.get(l);
        if (v!=null)
            label = v;
        else
            table.put(l,label);
    }

    public boolean isBlank() {
        return StringUtils.isBlank(toString());
    }

    public static String toString(TreeString t) {
        return t==null ? null : t.toString();
    }

    /**
     * Creates a {@link TreeString}.
     * Useful if you need to create one-off {@link TreeString} without {@link TreeStringBuilder}.
     * Memory consumption is still about the same to {@code new String(s)}.
     *
     * @return
     *      null if the parameter is null
     */
    public static TreeString of(String s) {
        if (s==null)    return null;
        return new TreeString(null,s);
    }

    /**
     * Default {@link Converter} implementation for XStream
     * that does interning scoped to one unmarshalling.
     */
    public static final class ConverterImpl implements Converter {
        public ConverterImpl(XStream xs) {
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.setValue(source==null?null:source.toString());
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            TreeStringBuilder builder = (TreeStringBuilder)context.get(TreeStringBuilder.class);
            if (builder==null) {
                context.put(TreeStringBuilder.class,builder=new TreeStringBuilder());

                // dedup at the end
                final TreeStringBuilder _builder = builder;
                context.addCompletionCallback(new Runnable() {
                    public void run() {
                        _builder.dedup();
                    }
                }, 0);
            }
            return builder.intern(reader.getValue());
        }

        public boolean canConvert(Class type) {
            return type==TreeString.class;
        }
    }
}
