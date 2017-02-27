// Solution for Googe Hashcode 2017 problem

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class noc {

	
	public static void main(String[] args) {
		
		
		// parse
		
		Scanner s = new Scanner(System.in);
		
		String[] nums = s.nextLine().split(" ");
		
		int V = Integer.parseInt(nums[0]),
				E = Integer.parseInt(nums[1]),
				R = Integer.parseInt(nums[2]),
				C = Integer.parseInt(nums[3]),
				X = Integer.parseInt(nums[4]);
		
		
		ArrayList<Integer> video_sizes = new ArrayList<Integer>();
		
		for (String i : s.nextLine().split(" ")) {
			video_sizes.add(Integer.parseInt(i));
		}
		
		ArrayList<endpoint> endpoints = new ArrayList<endpoint>();
		
		for (int i = 0; i < E; i++) {
			
			String[] LK = s.nextLine().split(" ");
			
			int L_d = Integer.parseInt(LK[0]),
					K = Integer.parseInt(LK[1]);
			
			ArrayList<Integer> cache_ids = new ArrayList<Integer>();
			ArrayList<Integer> latency = new ArrayList<Integer>();
			
			for (int j = 0; j < K; j++) {
				
				String[] cL = s.nextLine().split(" ");
				
				int c = Integer.parseInt(cL[0]),
						L_c = Integer.parseInt(cL[1]);
				
				cache_ids.add(c);
				latency.add(L_c);
			}
			
			endpoints.add(new endpoint(L_d, K, cache_ids, latency));
		}
		
		for (int i = 0; i < R; i++) {
			String[] line = s.nextLine().split(" ");
			
			int R_v = Integer.parseInt(line[0]),
					R_e = Integer.parseInt(line[1]),
					R_n = Integer.parseInt(line[2]);
			
			request r = new request(R_v, R_e, R_n);
			
			endpoints.get(R_e).requests.add(r);
		}
		
		// populate caches
		
		cache[] caches = new cache[C];
		
		for (int i = 0; i < C; i++) {
			caches[i] = new cache(i,V); // id 0.. (C-1)
		}
		
		for (endpoint e : endpoints) {
			for (int i = 0; i < e.cache_ids.size(); i++) {
				
				int cache_id = e.cache_ids.get(i);
				
				caches[cache_id].endpoints.add(e);
				caches[cache_id].latency.add(e.latency.get(i));
			}
		}
		
		// calculate video saved
		

		int cache_servers_used = 0;
		
		for (int it = 0; it < 1; it++) { // 1 pass is optimal for 3 of the sets
			
			cache_servers_used = 0;
		
		for (int i = 0; i < C; i++) { // for every cache
			
			
			caches[i].video_ids_to_output.clear();
			caches[i].space_used = 0;
			
			for (endpoint e : caches[i].endpoints) {
				
				for (request r : e.requests) {
					
					int latency = e.latency.get(e.cache_ids.indexOf(i));
					
					//if (r.lowest_latency <= latency && r.cache_placement != i)
						//continue;
					
					if (r.cache_placement == i) {
						r.cache_placement = -1;
						r.lowest_latency = Integer.MAX_VALUE;
					}
					
					caches[i].total_video_latency_saved[r.video_id].latency_saved += ((double)r.requests * Math.max(Math.min(e.L_c, r.lowest_latency) - latency, 0) / (double)(video_sizes.get(r.video_id)));
					
					if (r.lowest_latency <= latency)
						continue;
					
					r.lowest_latency = latency;
					r.cache_placement = i;
				}
				
			}
			
			Arrays.sort(caches[i].total_video_latency_saved);
			
			for (video_l v : caches[i].total_video_latency_saved) {
				
				if (caches[i].space_used + video_sizes.get(v.id) <= X) {
					//add
					caches[i].space_used += video_sizes.get(v.id);
					caches[i].video_ids_to_output.add(v.id);
				}
			}
			
			if (caches[i].video_ids_to_output.size() != 0) {
				cache_servers_used++;
			}
			
			// update requests
			
			for (endpoint e : endpoints) {
				
				for (request r : e.requests) {
					
					if (r.cache_placement == -1)
						continue;
					
					if (!caches[r.cache_placement].video_ids_to_output.contains(r.video_id)) {
						r.cache_placement = -1;
						r.lowest_latency = Integer.MAX_VALUE;
					}
				}
			}
		}
		}
		
		System.out.println(cache_servers_used);
		
		for (int i = 0; i < caches.length; i++) {
			
			
			if (caches[i].video_ids_to_output.size() != 0) {
				System.out.print(i); // id
				
				for (Integer video_id : caches[i].video_ids_to_output) {
					System.out.print(" " + video_id);
				}
				
				System.out.print("\n");
			}
		}
		
		
	}
}


class endpoint {
	
	int L_c; // latency to center
	int num_cache;
	
	ArrayList<request> requests = new ArrayList<request>();
	
	// id, latency
	//HashMap<Integer, Integer> cache_servers = new HashMap<Integer, Integer>();
	ArrayList<Integer> cache_ids = new ArrayList<Integer>();
	ArrayList<Integer> latency = new ArrayList<Integer>();
	
	
	endpoint(int L_c, int num_cache, ArrayList<Integer> cache_ids, ArrayList<Integer> latency) {
		this.L_c = L_c;
		this.num_cache = num_cache;
		this.cache_ids = cache_ids;
		this.latency = latency;
	}
}

class video_l implements Comparator<video_l>, Comparable<video_l> {
	double latency_saved = 0;
	int id;
	
	video_l (int id) {
		this.id = id;
	}
	
	@Override
	
	public int compare(video_l o1, video_l o2) {
		return (int)o2.latency_saved - (int)o1.latency_saved;
	}
	

	@Override
	public int compareTo(video_l o) {
		return (int)o.latency_saved - (int)this.latency_saved;
	}
}

class cache {
	
	video_l[] total_video_latency_saved; // for video id 0 .. 
	
	ArrayList<Integer> video_ids_to_output = new ArrayList<Integer>();
	
	//HashMap<endpoint, Integer> connected_endpoints = new HashMap<endpoint, Integer>();
	// endpoint, latency
	
	ArrayList<endpoint> endpoints = new ArrayList<endpoint>();
	ArrayList<Integer> latency = new ArrayList<Integer>(); // to endpoints above

	int space_used = 0;
	
	int id; // cache id
	
	cache(int id, int V) {
		this.id = id;
		total_video_latency_saved = new video_l[V];
		
		for (int i = 0; i<  V; i++)
			total_video_latency_saved[i] = new video_l(i);
	}
}

class request {
	
	int lowest_latency = Integer.MAX_VALUE;
	int cache_placement = -1;
	
	int video_id; // R_v ; for size
	int endpoint_id; // R_e
	int requests; // R_n ; amount
	
	endpoint end; // ?
	
	request(int R_v, int R_e, int R_n) {
		video_id = R_v;
		endpoint_id = R_e;
		requests = R_n;
		
	}
}
