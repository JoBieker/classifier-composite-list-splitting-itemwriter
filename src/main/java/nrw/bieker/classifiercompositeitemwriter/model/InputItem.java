package nrw.bieker.classifiercompositeitemwriter.model;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@RequiredArgsConstructor
public class InputItem {

    private Long id;
    private String name;

}
