/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.nantian.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import com.nantian.modules.system.domain.Dict;
import com.nantian.modules.system.domain.DictDetail;
import com.nantian.modules.system.repository.DictRepository;
import com.nantian.modules.system.service.dto.DictDetailQueryCriteria;
import com.nantian.utils.PageUtil;
import com.nantian.utils.QueryHelp;
import com.nantian.utils.RedisUtil;
import com.nantian.utils.ValidationUtil;
import com.nantian.modules.system.repository.DictDetailRepository;
import com.nantian.modules.system.service.DictDetailService;
import com.nantian.modules.system.service.dto.DictDetailDto;
import com.nantian.modules.system.service.mapstruct.DictDetailMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
* @author Zheng Jie
* @date 2019-04-10
*/
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictDetailServiceImpl implements DictDetailService {

    private final DictRepository dictRepository;
    private final DictDetailRepository dictDetailRepository;
    private final DictDetailMapper dictDetailMapper;
    private final RedisUtil redisUtil;

    @Override
    public Map<String,Object> queryAll(DictDetailQueryCriteria criteria, Pageable pageable) {
        Page<DictDetail> page = dictDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(dictDetailMapper::toDto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DictDetail resources) {
        dictDetailRepository.save(resources);
        // 清理缓存
        delCaches(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDetail resources) {
        DictDetail dictDetail = dictDetailRepository.findById(resources.getId()).orElseGet(DictDetail::new);
        ValidationUtil.isNull( dictDetail.getId(),"DictDetail","id",resources.getId());
        resources.setId(dictDetail.getId());
        dictDetailRepository.save(resources);
        // 清理缓存
        delCaches(resources);
    }

    @Override
    @Cacheable(key = "'name:' + #p0")
    public List<DictDetailDto> getDictByName(String name) {
        return dictDetailMapper.toDto(dictDetailRepository.findByDictName(name));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DictDetail dictDetail = dictDetailRepository.findById(id).orElseGet(DictDetail::new);
        // 清理缓存
        delCaches(dictDetail);
        dictDetailRepository.deleteById(id);
    }

    public void delCaches(DictDetail dictDetail){
        Dict dict = dictRepository.findById(dictDetail.getDict().getId()).orElseGet(Dict::new);
        redisUtil.del("dept::name:" + dict.getName());
    }
}
