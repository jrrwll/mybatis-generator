package $mapper_package;

import $entity_package.$entity_type;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * This class was generated by $generator_name
 *
 * @author $username
 * @version $date
 * @see <a href="https://github.com/jrrwll">Jerry Will's Github</a>
 */$at_mapper

public interface $mapper_type {

    int insert($entity_type entity);

    int insertSelective($entity_type entity);

    int batchInsert(List<$entity_type> entity);

    int delete($primary_key_declare_list);

    int deleteBy($condition_type condition);

    $entity_type select($primary_key_declare_list);

    List<$entity_type> selectBy($condition_type condition);

    long countBy($condition_type condition);

    int update($entity_type entity);

    int updateSelective($entity_type entity);

    int updateBy(@Param("entity") $entity_type entity, @Param("condition") $condition_type condition);

    int updateSelectiveBy(@Param("entity") $entity_type entity, @Param("condition") $condition_type condition);
}