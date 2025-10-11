import { Admin } from 'react-admin';
import React from 'react';
import polyglotI18nProvider from 'ra-i18n-polyglot';
import chineseMessages from '@haxqer/ra-language-chinese';

import restProvider from './provider/rest';
import { resource_list } from './resource'

export const i18nProvider = polyglotI18nProvider(
    locale => chineseMessages, 'zh'
);

const Index: React.FC = () => {
    return (
        <Admin dataProvider={restProvider}
               i18nProvider={i18nProvider} locale="zh">
            {resource_list}
        </Admin>
    );

};

export default Index;
