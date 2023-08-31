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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

import org.magnum.dataup.model.Video;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
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
	ArrayList<Video> savedVideos = new ArrayList<Video>();

	@RequestMapping("/")
	public ResponseEntity<String> main_page(){
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("access-control-allow-origin", "*");
		responseHeaders.set("access-control-expose-headers", "*");
		//Video someVideo = new Video();		
		return new ResponseEntity<String>("hello", responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/video", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<String> postVideoAttributes(@RequestBody Video video){
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

		System.out.println("Request recieved");
		System.out.println("video id: ".concat( String.valueOf(video.getId()) ));
		System.out.println("video title: ".concat( video.getTitle() ));
		System.out.println("video contentType: ".concat( video.getContentType() ));		

		savedVideos.add(video);
		System.out.println("video saved");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("access-control-allow-origin", "*");
		responseHeaders.set("access-control-expose-headers", "*");

		return new ResponseEntity<String>("hello", responseHeaders , HttpStatus.OK);
	}
	
}
