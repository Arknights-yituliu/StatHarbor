package org.yituliu.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.yituliu.entity.dto.UniversalDataDTO;
import org.yituliu.entity.po.ProjectCredential;
import org.yituliu.entity.po.UniversalData;
import org.yituliu.mapper.ProjectCredentialMapper;
import org.yituliu.mapper.UniversalDataMapper;
import org.yituliu.common.enums.ResultCode;
import org.yituliu.common.exception.ServiceException;
import org.yituliu.common.utils.IdGenerator;
import org.yituliu.common.utils.JsonMapper;

import java.util.Date;
import java.util.List;

@Service
public class UniversalDataSerivce {

    private static final String REDIS_TOKEN_PREFIX = "api:key:";

    private final UniversalDataMapper universalDataMapper;
    private final ProjectCredentialMapper projectCredentialMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IdGenerator idGenerator;

    public UniversalDataSerivce(UniversalDataMapper universalDataMapper,
            ProjectCredentialMapper projectCredentialMapper,
            RedisTemplate<String, Object> redisTemplate) {
        this.universalDataMapper = universalDataMapper;
        this.projectCredentialMapper = projectCredentialMapper;
        this.redisTemplate = redisTemplate;
        this.idGenerator = new IdGenerator(1L);
    }

    /**
     * 将UniversalDataDTO转换为UniversalData实体
     * 
     * @param dto 前端传入的数据传输对象
     * @return 数据库实体对象
     */
    public UniversalData convertToEntity(UniversalDataDTO dto) {
        validateDTO(dto);

        UniversalData entity = new UniversalData();
        entity.setId(idGenerator.nextId());
        entity.setProjectKey(dto.getProjectKey());
        entity.setCategory(dto.getCategory());
        entity.setVersion(dto.getVersion());
        entity.setSource(dto.getSource());
        Object payload = dto.getPayload();
        entity.setPayload(JsonMapper.toJSONString(payload));
        entity.setReceivedAt(new Date());
        entity.setNote(dto.getNote());

        String capturedAt = dto.getCapturedAt();

        if (capturedAt != null && !capturedAt.isEmpty()) {
            try {
                long timestamp = Long.parseLong(capturedAt);
                if (timestamp < 0) {
                    throw new ServiceException(ResultCode.PARAM_IS_INVALID);
                }
                entity.setCapturedAt(new Date(timestamp));
            } catch (NumberFormatException e) {
                throw new ServiceException(ResultCode.PARAM_INVALID);
            }
        }

        return entity;
    }

    /**
     * 校验UniversalDataDTO必填字段
     * 
     * @param dto 前端传入的数据传输对象
     */
    private void validateDTO(UniversalDataDTO dto) {
        if (dto.getProjectKey() == null || dto.getProjectKey().isEmpty()) {
            throw new ServiceException(ResultCode.PROJECT_KEY_IS_BLANK);
        }
        if (dto.getCategory() == null || dto.getCategory().isEmpty()) {
            throw new ServiceException(ResultCode.CATEGORY_IS_BLANK);
        }
        if (dto.getVersion() == null || dto.getVersion().isEmpty()) {
            throw new ServiceException(ResultCode.VERSION_IS_BLANK);
        }
        if (dto.getSource() == null || dto.getSource().isEmpty()) {
            throw new ServiceException(ResultCode.SOURCE_IS_BLANK);
        }
        if (dto.getPayload() == null) {
            throw new ServiceException(ResultCode.PAYLOAD_IS_BLANK);
        }
        
    }

    /**
     * 上传通用数据 Stat Harbor Upload
     * 
     * @param universalDataDTO
     */
    public void uploadUniversalDataCollection(UniversalDataDTO universalDataDTO) {

        // 将universalDataDTO转换为UniversalData实体
        UniversalData universalData = convertToEntity(universalDataDTO);
        // 保存UniversalData实体到数据库
        universalDataMapper.insert(universalData);
        // 记录上传日志

    }

    /**
     * 从数据库加载所有项目凭证的secretKey到Redis
     * Redis key格式: api:token:{secretKey}
     */
    public void loadProjectCredentialsToRedis() {
        List<ProjectCredential> credentials = projectCredentialMapper.selectList(null);

        for (ProjectCredential credential : credentials) {
            String secretKey = credential.getSecretKey();
            if (secretKey != null && !secretKey.isEmpty()) {
                redisTemplate.opsForValue().set(REDIS_TOKEN_PREFIX + secretKey, credential.getProjectKey());
            }
        }
    }
}
