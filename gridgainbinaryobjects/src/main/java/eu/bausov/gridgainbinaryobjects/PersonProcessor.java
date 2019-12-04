package eu.bausov.gridgainbinaryobjects;

import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.CacheEntryProcessor;

import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

/**
 * Created by GreenNun on 04.12.2019.
 */
public class PersonProcessor implements CacheEntryProcessor<Integer, BinaryObject, Object> {
    @Override
    public Object process(MutableEntry<Integer, BinaryObject> entry, Object... arguments) throws EntryProcessorException {
        // Create a builder from the old value.
        BinaryObjectBuilder bldr = entry.getValue().toBuilder();

        //Update the field in the builder.
        bldr.setField("name", arguments[0]);

        // Set new value to the entry.
        entry.setValue(bldr.build());

        return null;
    }
}
