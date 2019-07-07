package com.example.mapdemo;

import android.os.AsyncTask;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        assertEquals(4, 2 + 2);
    }
    @Test
    public void test_upload()
    {

        UploadList uploadList = new UploadList();

        uploadList.execute((long) 1);
        assertEquals(4, 2 + 2);
    }

    private class UploadList extends AsyncTask<Long, Integer, String> {

        private String retSrc;

        @Override
        protected String doInBackground(Long... iId) {

            Long plan_id = iId[0];

            //Upload upload1 = new Upload();
          //  retSrc =upload1.MakeUpload(plan_id, fa);

          //  upload1.uploadPosition(33.8,44.8);


            return retSrc;
        }




        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
          //
        }

    }

}