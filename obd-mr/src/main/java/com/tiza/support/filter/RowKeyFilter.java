package com.tiza.support.filter;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Description: RowKeyFilter
 * Author: DIYILIU
 * Update: 2018-09-03 16:54
 */
public class RowKeyFilter extends FilterBase {

    private long begin;
    private long end;

    private boolean filterRow = true;

    public RowKeyFilter(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public ReturnCode filterKeyValue(Cell v) {

        return filterRow ? ReturnCode.NEXT_ROW : ReturnCode.INCLUDE;
    }

    @Override
    public boolean filterRowKey(byte[] buffer, int offset, int length) {
        if(length != 12){
            throw new IllegalArgumentException("length value must is 12");
        }

        byte[] rowKey = Bytes.copy(buffer, offset, length);
        if(rowKey.length != 12){
            throw new IllegalStateException("rowKey length must is 12");
        }

        long time = Bytes.toLong(rowKey, 4);
        if (time >= begin && time < end) {

            filterRow = false;
        }

        return filterRow;
    }

    @Override
    public void reset() {

        filterRow = true;
    }


    @Override
    public boolean filterRow() {

        return filterRow;
    }


    @Override
    public byte[] toByteArray() {

        return Bytes.add(Bytes.toBytes(begin), Bytes.toBytes(end));
    }

    public static RowKeyFilter parseFrom(final byte[] bytes) throws DeserializationException {

        return new RowKeyFilter(Bytes.toLong(bytes), Bytes.toLong(bytes, 8));
    }

    /**
     * 标记是否进行序列化
     */
    public boolean areSerializedFieldsEqual(Filter o) {
        if (o == this || o instanceof RowKeyFilter) {
            return true;
        }

        return false;
    }
}
