import simpleRestProvider from 'ra-data-simple-rest';
import type { Options } from 'react-admin'

const httpClient = (url: string, options: Options = {}) => {
    options.headers = new Headers(options.headers);
    options.headers.set('Content-Type', 'application/json');
    options.headers.set('Accept', 'application/json');

    return fetch(url, options).then(res => {
        if (res.status < 200 || res.status >= 300) {
            return Promise.reject(new Error(res.statusText));
        }
        return res.json();
    });
};

export const api_url: string = import.meta.env.VITE_API_URL || '/api/v1';

const customDataProvider = {
    ...simpleRestProvider(api_url),
    getList: async (resource: string, params: any) => {
        const { page, perPage } = params.pagination;
        const { field, order } = params.sort;
        const query = {
            pageSize: perPage,
            pageNo: page,
            sortField: field,
            sortOrder: order.toLowerCase(),
            ...params.filter,
        };

        const url = `${api_url}/${resource}/list?${new URLSearchParams(query)}`;
        const json = await httpClient(url);
        const err_msg = get_err_msg(json);
        if (err_msg != null) {
            return Promise.reject(new Error(`${err_msg}`));
        }
        const data = json.data
        if (Array.isArray(data)) {
            return {
                data: data,
                total: data.length,
            }
        } else {
            return {
                data: data.items || data.list,
                total: data.total || data.totalCount,
            }
        }
    },
    create: async (resource: string, params: any) => {
        const json = await httpClient(`${api_url}/${resource}`, {
            method: 'POST',
            body: JSON.stringify(params.data),
        });
        const err_msg = get_err_msg(json);
        if (err_msg != null) {
            return Promise.reject(new Error(`${err_msg}`));
        }
        return { data: json.data };
    },
    update: async (resource: string, params: any) => {
        const json = await httpClient(`${api_url}/${resource}`, {
            method: 'PUT',
            body: JSON.stringify({ id: params.id, ...params.data }),
        });
        const err_msg = get_err_msg(json);
        if (err_msg != null) {
            return Promise.reject(new Error(`${err_msg}`));
        }
        return { data: json.data };
    },
    delete: async (resource: string, params: any) => {
        const json = await httpClient(`${api_url}/${resource}?id=${params.id}`, {
            method: 'DELETE',
        });
        const err_msg = get_err_msg(json);
        if (err_msg != null) {
            return Promise.reject(new Error(`${err_msg}`));
        }
        return { data: json.data };
    },
    getOne: async (resource: string, params: any) => {
        const json = await httpClient(`${api_url}/${resource}?id=${params.id}`);
        const err_msg = get_err_msg(json);
        if (err_msg != null) {
            return Promise.reject(new Error(`${err_msg}`));
        }
        return { data: json.data };
    },
    getMany: async (resource: string, params: any) => {
        const ids_query = params.ids.map((id: any)=> `id=${id}`).join("&")
        const json = await httpClient(`${api_url}/${resource}/getMany?${ids_query}`);
        const err_msg = get_err_msg(json);
        if (err_msg != null) {
            return Promise.reject(new Error(`${err_msg}`));
        }
        const data = json.data
        if (Array.isArray(data)) {
            return { data: data };
        } else {
            return { data: data.items || data.list };
        }
    },
};

function get_err_msg(json: any): string | null {
    // {err_code, err_args, data}
    if (json.err_code !== undefined && json.err_code != 'ok') {
        if (json.err_args && json.err_args.msg) {
            return json.err_args.msg;
        }
        return JSON.stringify(json);
    }
    // {success, msg|message, data}
    if (json.success !== undefined && !json.success) {
        return json.msg || json.message;
    }
    // {code, msg|message, data}
    if (json.code !== undefined && json.code != 200) {
        return json.msg || json.message;
    }
    return null;
}

export default customDataProvider;
