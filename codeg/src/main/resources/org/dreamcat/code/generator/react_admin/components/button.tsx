import { DeleteWithConfirmButton, useRecordContext } from 'react-admin'
import * as React from 'react'

export const CustomDeleteButton = ({ name, ...props }) => {
    const record = useRecordContext();

    return (
        <DeleteWithConfirmButton
            mutationMode="pessimistic"
            confirmTitle={`删除 ${name} ${record?.name || ''}`}
            confirmContent={`确定删除吗？`}
            successMessage="删除成功！"
            {...props}
        />
    );
};