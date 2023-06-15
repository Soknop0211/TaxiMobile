package com.eazy.daiku.utility.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class MCoroutineAsyncTask<Params, Progress, Result> {
    abstract fun onPreExecute()

    abstract fun doInBackground(vararg params: Params?): Result

    abstract fun onProgressUpdate(vararg progress: Progress?)

    abstract fun onPostExecute(result: Result?)

    abstract fun onCanceled(result: Result?)

    protected var isCancelled = false

    fun publishProgress(vararg progresses: Progress?) {
        GlobalScope.launch(Dispatchers.Main) {
            onProgressUpdate(*progresses)
        }
    }

    fun execute(vararg params: Params?) {
        onPreExecute()

        GlobalScope.launch (Dispatchers.Default){
            var result = doInBackground(*params)
            withContext(Dispatchers.Main){
                onPostExecute(result)
            }
        }
    }

    fun cancel(myInterruptIfRunning: Boolean) {

    }
}