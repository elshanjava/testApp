package packageApp.Information;

import org.springframework.batch.item.ItemProcessor;

public class InformationItemProcessor implements ItemProcessor<Information, Information> {
    @Override
    public Information process(final Information information) throws Exception {
        final int id = information.getId();
        final String name = information.getName();
        final double value = information.getValue();

        return new Information(id,name,value);
    }
}
