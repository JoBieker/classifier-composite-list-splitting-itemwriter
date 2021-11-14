package nrw.bieker.classifiercompositeitemwriter.config;

import lombok.RequiredArgsConstructor;
import nrw.bieker.classifiercompositeitemwriter.model.Item;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

@RequiredArgsConstructor
public class InputItemClassifier implements Classifier<Item, ItemWriter<? super Item>> {

    private final ItemWriter<Item> evenItemWriter;
    private final ItemWriter<Item> oddItemWriter;

    @Override
    public ItemWriter<Item> classify(Item item) {
        if(item.getId() % 2 == 0) {
            return evenItemWriter;
        }
        else {
            return oddItemWriter;
        }
    }
}
