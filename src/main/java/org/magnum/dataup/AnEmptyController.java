/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.mapping.Map;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.msgpack.io.Input;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import retrofit.mime.TypedInput;

@RestController
public class AnEmptyController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	ArrayList<Video> savedVideos;
	HashMap<Long, Video> idToVideo;
	VideoFileManager videoFileManager;
	
	public AnEmptyController(){
		savedVideos = new ArrayList<Video>();
		idToVideo = new HashMap<Long, Video>();
		try{
			videoFileManager = VideoFileManager.get();
		} catch( Exception e ){
			System.out.println("Unable to create video file manager");
		}
	}	

	private String getDataUrl(long videoId){
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

    private String getUrlBaseForLocalServer() {
		HttpServletRequest request = 
			((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base = 
			"http://"+request.getServerName() 
			+ ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		return base;
	}

	@RequestMapping("/")
	public ResponseEntity<String> main_page(){
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("access-control-allow-origin", "*");
		responseHeaders.set("access-control-expose-headers", "*");
		//Video someVideo = new Video();		
		return new ResponseEntity<String>("hello", responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/video/{videoId}/data", method = RequestMethod.POST, consumes = "multipart/form-data", produces = "application/json")
	public VideoStatus postVideoData(
		@PathVariable Long videoId, 
		@RequestParam("data") MultipartFile videoData,
		HttpServletResponse response){						
				
		Video videoMetadata = idToVideo.get(videoId);
		if (videoMetadata == null){
			response.setStatus(404);			
			return null;
		}else{
			try{			
				videoFileManager.saveVideoData(videoMetadata, videoData.getInputStream());
				return new VideoStatus(VideoState.READY);
			} catch( Exception e ){
				System.out.println("Unable to save video data");
				response.setStatus(400);				
				return null;
			}
		}		
	}

	@RequestMapping(value = "/video/{videoId}/data", method = RequestMethod.GET)
	public void getVideoData(
		@PathVariable Long videoId,		
		HttpServletResponse response){						
				
		Video videoMetadata = idToVideo.get(videoId);
		if (videoMetadata == null){
			response.setStatus(404);						
		}else{									
			try{			
				response.setStatus(200);
				videoFileManager.copyVideoData(videoMetadata, response.getOutputStream());										
			} catch( Exception e ){				
				response.setStatus(404);
			}
		}		
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.GET, produces = "application/json")
	public ArrayList<Video> getVideos(){
		return savedVideos;		
	}

	@RequestMapping(value = "/video", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public Video postVideoAttributes(@RequestBody Video video){
		//@RequestBody Video video
		long biggestID;
		try{
			Video lastVideoAdded = savedVideos.get( savedVideos.size() -1 );
			biggestID = lastVideoAdded.getId();
		}
		catch(Exception e){
			biggestID = 0;
		}
		video.setId(biggestID + 1);
		video.setDataUrl(getDataUrl(biggestID + 1));

		System.out.println("Request recieved");
		System.out.println("video id: ".concat( String.valueOf(video.getId()) ));
		System.out.println("video title: ".concat( video.getTitle() ));
		System.out.println("video contentType: ".concat( video.getContentType() ));		

		savedVideos.add(video);
		idToVideo.put(video.getId(), video);
		System.out.println("video saved");

		/*
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("access-control-allow-origin", "*");
		responseHeaders.set("access-control-expose-headers", "*");

		return new ResponseEntity<String>("hello", responseHeaders , HttpStatus.OK);
		 */
		return video;
	}
	
}
