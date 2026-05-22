package org.yituliu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yituliu.common.utils.Result;
import org.yituliu.entity.dto.UniversalDataDTO;



@RestController

public class UniversalDataCollectionController {
    
    /**
     * 上传通用数据  Stat Harbor Upload
     * @return
     */
    @PostMapping("/api/v1/records")
    public Result<String> uploadUniversalDataCollection(UniversalDataDTO universalDataDTO) {
        return Result.success("上传成功");
    }

    
}
