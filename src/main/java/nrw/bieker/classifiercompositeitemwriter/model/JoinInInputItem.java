package nrw.bieker.classifiercompositeitemwriter.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class JoinInInputItem {

    private Long id;
    private String adresse;
    private Long personId;
}
