package com.victor.spider.core.downloader;

import com.victor.spider.core.Page;
import com.victor.spider.core.Request;
import com.victor.spider.core.Task;

/**
 * Downloader is the part that downloads web pages and store in Page object. <br>
 * Downloader has {@link #setThread(int)} method because downloader is always the bottleneck of a crawler,
 * there are always some mechanisms such as pooling in downloader, and pool size is related to thread numbers.
 */
public interface Downloader {

    /**
     * Downloads web pages and store in Page object.
     *
     * @param request
     * @param task
     * @return page
     */
    public Page download(Request request, Task task);

    /**
     * Tell the downloader how many threads the spider used.
     * @param threadNum number of threads
     */
    public void setThread(int threadNum);
}
