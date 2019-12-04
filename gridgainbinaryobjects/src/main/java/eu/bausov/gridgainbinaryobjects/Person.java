package eu.bausov.gridgainbinaryobjects;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by GreenNun on 04.12.2019.
 */
@Data
@AllArgsConstructor
public class Person implements Serializable {
    private static final long serialVersionUID = 2311419643721465270L;
    private int age;
    private String name;
}
