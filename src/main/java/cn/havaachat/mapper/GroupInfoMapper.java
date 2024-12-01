package cn.havaachat.mapper;

import cn.havaachat.annotation.AutoFill;
import cn.havaachat.enums.OperationTypeEnum;
import cn.havaachat.pojo.entity.GroupInfo;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface GroupInfoMapper {
    @Select("select * from group_info where group_id=#{groupId}")
    GroupInfo findById(String groupId);

    /**
     * 查询当前用户已创建群组数
     * @param groupOwnerId
     * @return
     */
    @Select("select count(*) from group_info where group_owner_id=#{groupOwnerId} and status=#{1}")
    Integer countByGroupOwnerId(String groupOwnerId);

    /**
     * 更新
     * @param groupInfo
     */
    @AutoFill(OperationTypeEnum.UPDATE)
    void update(GroupInfo groupInfo);

    /**
     * 插入
     * @param groupInfo
     */
    @AutoFill(OperationTypeEnum.INSERT)
    void insert(GroupInfo groupInfo);

    /**
     * 根据群主用户id批量查找
     * @param groupOwnerId
     * @return
     */
    @Select("select * from group_info where group_owner_id=#{groupOwnerId} and status=#{status} order by create_time desc")
    List<GroupInfo> findBatchByGroupOwnerIdAndStatus(String groupOwnerId,Integer status);

    /**
     * 分页批量查找
     * @return
     */
    Page<GroupInfo> findBatch(GroupInfo groupInfo);

}
