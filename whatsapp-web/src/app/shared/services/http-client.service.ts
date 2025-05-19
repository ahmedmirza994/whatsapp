import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
	providedIn: 'root',
})
export class HttpClientService {
	constructor(private http: HttpClient) {}

	private handleError(error: HttpErrorResponse): Observable<never> {
		console.error('Error:' + error);
		let errorMessage = 'An unknown error occurred!';
		if (error.error instanceof ErrorEvent) {
			errorMessage = `${error.error.message}`;
		} else {
			const apiResponse = error.error as ApiResponse<any>;
			if (apiResponse && apiResponse.error) {
				errorMessage = apiResponse.error;
			}
		}
		return throwError(() => new Error(errorMessage));
	}

	get<T>(
		url: string,
		options?: { headers?: HttpHeaders; params?: HttpParams }
	): Observable<ApiResponse<T>> {
		return this.http.get<ApiResponse<T>>(url, options).pipe(
			map(response => {
				if (response.status === 200 && response.data) {
					return response;
				} else {
					throw new Error(response.error || 'Request failed');
				}
			}),
			catchError(this.handleError)
		);
	}

	getBlob(
		url: string,
		options?: {
			headers?: HttpHeaders;
			params?: HttpParams;
		}
	): Observable<Blob> {
		const requestOptions = {
			...options,
			responseType: 'blob' as 'blob',
		};
		return this.http.get(url, requestOptions).pipe(
			map(response => {
				if (response instanceof Blob) {
					return response;
				} else {
					throw new Error('Request failed');
				}
			}),
			catchError(this.handleError)
		);
	}

	post<T>(
		url: string,
		body: any,
		options?: { headers?: HttpHeaders }
	): Observable<ApiResponse<T>> {
		const processedBody = body instanceof FormData ? body : this.preprocessRequestBody(body);

		// For FormData, Angular's HttpClient typically sets the Content-Type automatically
		// with the correct boundary. Explicitly setting it can cause issues.
		// If options.headers are provided and include Content-Type, it might need to be removed for FormData.
		let requestOptions = options;
		if (body instanceof FormData && options?.headers?.has('Content-Type')) {
			// Clone headers and remove Content-Type if it's FormData
			const clonedHeaders = options.headers.delete('Content-Type');
			requestOptions = { ...options, headers: clonedHeaders };
		}

		console.log('Request URL:', url);
		console.log('Request Body:', processedBody);
		return this.http.post<ApiResponse<T>>(url, processedBody, requestOptions).pipe(
			map(response => {
				if (response.status === 200 && response.data) {
					return response;
				} else {
					throw new Error(response.error || 'Request failed');
				}
			}),
			catchError(this.handleError)
		);
	}

	put<T>(
		url: string,
		body: any,
		options?: { headers?: HttpHeaders }
	): Observable<ApiResponse<T>> {
		const processedBody = this.preprocessRequestBody(body);
		return this.http.put<ApiResponse<T>>(url, processedBody, options).pipe(
			map(response => {
				if (response.status === 200 && response.data) {
					return response;
				} else {
					throw new Error(response.error || 'Request failed');
				}
			}),
			catchError(this.handleError)
		);
	}

	delete<T>(url: string, options?: { headers?: HttpHeaders }): Observable<ApiResponse<T>> {
		return this.http.delete<ApiResponse<T>>(url, options).pipe(
			map(response => {
				if (response.status === 200 && response.data) {
					return response;
				} else {
					throw new Error(response.error || 'Request failed');
				}
			}),
			catchError(this.handleError)
		);
	}

	private preprocessRequestBody(body: any): any {
		if (body && typeof body === 'object' && !(body instanceof FormData)) {
			return Object.keys(body).reduce((acc: { [key: string]: any }, key) => {
				acc[key] = body[key] === '' ? null : body[key];
				return acc;
			}, {});
		}
		return body;
	}
}
