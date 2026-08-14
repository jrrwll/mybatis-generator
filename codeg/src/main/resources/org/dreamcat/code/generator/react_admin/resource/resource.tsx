import {
    Resource, Filter, List, Datagrid, Edit, Create, SimpleForm,
    useRecordContext,
    TextField, NumberField, DateField, BooleanField,
    TextInput, NumberInput, DateInput, DateTimeInput, BooleanInput, SelectArrayInput, ReferenceArrayInput,
    EditButton, DeleteButton
} from 'react-admin';
import * as React from "react";
import { CustomDeleteButton } from '../components/button'


export const ${class_name}Filter = (props: any) => (
    <Filter {...props}>
$filter_list
    </Filter>
);

export const ${class_name}List = () => (
    <List title={${display_name}} perPage={20} filters={<${class_name}Filter />}>
        <Datagrid>
$list_fields
            <EditButton/>
            <CustomDeleteButton name={"${resource_name}"}/>
        </Datagrid>
    </List>
);

const ${class_name}Title = () => {
    const record = useRecordContext();
    return <span>{record ? `"\${record.${title_field}}"` : ''}</span>;
};

export const ${class_name}Edit = () => (
    <Edit title={<${class_name}Title/>}>
        <SimpleForm>
$edit_fields
        </SimpleForm>
    </Edit>
);

export const ${class_name}Create = () => (
    <Create title="${display_name}">
        <SimpleForm>
$create_fields
        </SimpleForm>
    </Create>
);

const ${class_name}Resource = () => (
    <Resource name="${resource_name}"
              list={${class_name}List}
              edit={${class_name}Edit}
              create={${class_name}Create}
    />
)

export default ${class_name}Resource;
