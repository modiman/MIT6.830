package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;

        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }
    private  Type[] typeAr;
    private  String[] fieldAr;

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here

        return new Iterator<TDItem>() {
            private int index = -1;
            @Override
            public boolean hasNext() {
                return index + 1 < typeAr.length ;
            }

            @Override
            public TDItem next() {
                if (++index == typeAr.length) {
                    throw new NoSuchElementException();
                } else {
                    return new TDItem(typeAr[index], fieldAr[index]);
                }
            }
        };
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {

        // some code goes here

        this.typeAr = typeAr;
        this.fieldAr = fieldAr;
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        this(typeAr,new String[typeAr.length]);
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return typeAr == null ? 0 : typeAr.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if (fieldAr==null || i >= fieldAr.length || i<0 ) throw new NoSuchElementException("Index Out of Range");
        else return fieldAr[i];
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        return typeAr[i];
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        if (fieldAr == null || name == null) throw new NoSuchElementException("Field Name is Empty");
        int index = 0;
        for (String s:fieldAr){
            if(name.equals(s)) return index;
            index++;
        }
//        for (int i = 0;i<fieldAr.length;i++){
//            if(name.equals(fieldAr[i])) return i;
//        }
        throw new NoSuchElementException("No Such Field Name");

    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int size = 0;
        if (typeAr == null || typeAr.length==0) return 0;


        for (int i=0; i<typeAr.length; i++) {
            size += typeAr[i].getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        Type[]types = new Type[td1.typeAr.length + td2.typeAr.length];
        String[]names = new String[td1.typeAr.length + td2.typeAr.length];
        int i = 0;
        for (int j=0;j<td1.typeAr.length;j++){
            types[i] = td1.typeAr[j];
            names[i] = td1.fieldAr[j];
            i++;
        }
        for (int j=0;j<td2.typeAr.length;j++){
            types[i] = td2.typeAr[j];
            names[i] = td2.fieldAr[j];
            i++;
        }
        return new TupleDesc(types,names);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     *
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        // some code goes here
        if (!(o instanceof TupleDesc)) {
            return false;
        } else {
            TupleDesc other = (TupleDesc)o;

            if (this.numFields() != other.numFields()) {
                return false;
            } else {
                int n = this.numFields();

                for (int i=0; i<n; i++) {
                    if (null == this.getFieldName(i)) {
                        if (null != other.getFieldName(i)) {
                            return false;
                        }
                    } else if (this.getFieldName(i).equals(other.getFieldName(i))) {
                        return false;
                    } else if (this.getFieldType(i) != other.getFieldType(i)) { // Will this work ?
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        StringBuilder sb = new StringBuilder();
        int n = this.numFields();

        sb.append(getFieldType(0));
        sb.append("("+this.getFieldName(0)+")");
        for (int i=1; i<n; i++) {
            sb.append(","+getFieldType(i));
            sb.append("("+this.getFieldName(i)+")");
        }
        return sb.toString();

    }
}
