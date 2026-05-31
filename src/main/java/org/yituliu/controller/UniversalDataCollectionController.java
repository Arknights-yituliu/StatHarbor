package org.yituliu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.yituliu.common.utils.Result;
import org.yituliu.entity.dto.UniversalDataDTO;
import org.yituliu.service.UniversalDataSerivce;



@RestController

public class UniversalDataCollectionController {
    private final UniversalDataSerivce universalDataSerivce;

    public UniversalDataCollectionController(UniversalDataSerivce universalDataSerivce) {
        this.universalDataSerivce = universalDataSerivce;
    }
    /**
     * 上传通用数据  Stat Harbor Upload
     * @return
     */
    @PostMapping("/v1/records")
    public Result<String> uploadUniversalDataCollection(@RequestBody UniversalDataDTO universalDataDTO) {
        universalDataSerivce.uploadUniversalDataCollection(universalDataDTO);
        return Result.success("上传成功");
    }

    /**
     * 写入项目凭证到Redis
     * @return
     */
    @PostMapping("/v1/set-key")
    public Result<String> setKey() {
        universalDataSerivce.loadProjectCredentialsToRedis();
        return Result.success("写入key成功");
    }
}
