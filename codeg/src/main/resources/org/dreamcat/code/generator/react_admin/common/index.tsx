import { Admin } from 'react-admin';
import React from 'react';

import restProvider from './provider/rest';
import { resource_list } from './resource'


const Index: React.FC = () => {
    return (
        <Admin dataProvider={restProvider}>
            {resource_list}
        </Admin>
    );
};

export default Index;
