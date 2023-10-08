package org.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class NASAMediaLibrary {

    private static final String BASE_URL = "https://images-api.nasa.gov";

    public void searchNASA(String keywords, String year, String mediaType, String page_size) {
        String endpoint = BASE_URL + "/search";
        String url = endpoint + "?keywords=" + keywords + "&year_start=" + year + "&year_end=" + year + "&media_type=" + mediaType + "&page=1&page_size=" + page_size;
        String responseData = "";

        try {
            responseData = getResponseData(url);
        } catch (IOException e) {
            System.err.println("Error occurred while searching NASA: " + e.getMessage());
        }

        JSONObject json = new JSONObject(responseData);
        JSONArray items = json.getJSONObject("collection").getJSONArray("items");

        parseJson(items, mediaType);
    }

    public void parseJson(JSONArray items, String mediaType) {
        List<String> videoSuffixes = Arrays.asList(".mp4", ".avi", ".mov", ".mkv", ".wmv");
        List<String> imageSuffixes = Arrays.asList(".jpg", ".jpeg", ".png", ".bmp", ".tif");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String title = item.getJSONArray("data").getJSONObject(0).getString("title");
            String jsonCollectionUrl = item.getString("href");
            String responseData = "";

            try {
                responseData = getResponseData(jsonCollectionUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = new JSONArray(responseData);

            for (int j = 0; j < jsonArray.length(); j++) {
                String url = jsonArray.getString(j);

                if (mediaType.equals("video") && videoSuffixes.stream().anyMatch(url::endsWith)) {
                    printItem(title, url);
                } else if (mediaType.equals("image") && imageSuffixes.stream().anyMatch(url::endsWith)) {
                    printItem(title, url);
                    break;
                }
            }
        }
    }

    public String getResponseData(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body()).string();
            } else {
                System.out.println("Request was not successful.");
                return null;
            }
        }
    }

    public void printItem(String title, String url) {
        System.out.println(title);
        System.out.println(url);
        System.out.println("---");
    }

    public static void main(String[] args) {
        NASAMediaLibrary nasaLibrary = new NASAMediaLibrary();

        System.out.println("5 Images about/from Mars from 2018:");
        nasaLibrary.searchNASA("mars", "2018", "image", "5");
        System.out.println();

        System.out.println("All Video links from first 5 results about Mars from 2018:");
        nasaLibrary.searchNASA("mars", "2018", "video", "5");
    }
}
