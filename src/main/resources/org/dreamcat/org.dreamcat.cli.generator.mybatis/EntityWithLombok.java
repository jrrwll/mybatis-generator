package $entity_package;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
$extra_import

/**
 * This class was generated by $generator_name
 *
 * @author $username
 * @version $date
 * @see <a href="https://github.com/jrrwll">Jerry Will's Github</a>
 */
@Getter
@Setter
@ToString
public class $entity_type implements Serializable {

    private static final long serialVersionUID = 1L;

    // /**
    //  *  $comment
    //  */
    // private $type $property;

    $property_declare_list
}