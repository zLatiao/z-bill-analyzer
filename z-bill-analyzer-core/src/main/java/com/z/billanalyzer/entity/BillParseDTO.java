package com.z.billanalyzer.entity;

import com.z.billanalyzer.enums.BillSourceEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

/**
 * @author z-latiao
 * @since 2025/2/21 10:03
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class BillParseDTO {
    private BillSourceEnum billSourceEnum;
    private File file;
}
