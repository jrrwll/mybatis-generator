package $mapper_package;

import $condition_package.$condition_type;
import $entity_package.$entity_type;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
$extra_import

/**
 * This class was generated by $generator_name
 *
 * @author $username
 * @version $date
 * @see <a href="https://github.com/jrrwll">Jerry Will's Github</a>
 */
@Mapper
public interface $mapper_type {

    int insert($entity_type entity);

    int insertSelective($entity_type entity);

    int batchInsert(List<$entity_type> entity);

    int delete($primary_key_declare_list);

    int deleteWhere($condition_type condition);

    $entity_type select($primary_key_declare_list);

    List<$entity_type> selectBy($condition_type condition);

    long countWhere($condition_type condition);

    int update($entity_type entity);

    int updateSelective($entity_type entity);

    int updateWhere(@Param("entity") $entity_type entity, @Param("condition") $condition_type condition);

    int updateSelectiveWhere(@Param("entity") $entity_type entity, @Param("condition") $condition_type condition);
}