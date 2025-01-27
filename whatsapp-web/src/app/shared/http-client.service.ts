import { Injectable } from '@angular/core';
import {
	HttpClient,
	HttpErrorResponse,
	HttpHeaders,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse } from './api-response.model';

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
		options?: { headers?: HttpHeaders }
	): Observable<ApiResponse<T>> {
		return this.http.get<ApiResponse<T>>(url, options).pipe(
			map((response) => {
				if (response.status === 200 && response.data) {
					return response;
				} else {
					throw new Error(response.error || 'Request failed');
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
		const processedBody = this.preprocessRequestBody(body);
		return this.http.post<ApiResponse<T>>(url, processedBody, options).pipe(
			map((response) => {
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
		if (body && typeof body === 'object') {
			return Object.keys(body).reduce(
				(acc: { [key: string]: any }, key) => {
					acc[key] = body[key] === '' ? null : body[key];
					return acc;
				},
				{}
			);
		}
		return body;
	}
}
