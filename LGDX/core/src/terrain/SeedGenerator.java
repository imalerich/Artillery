package terrain;

public class SeedGenerator 
{
	private static void GenerateData(TerrainSeed Seed, int WorldW, int WorldH, int R, int G, int B)
	{
		// set the basic properties of the generated terrain
		Seed.SetDimmensions(WorldW, WorldH);
		Seed.SetConsistencyLevel( 4 + (int)(Math.random()*3) );
		Seed.SetSoftness( (int)(Math.random()*64) + 32 );
		Seed.SetMinLevel(64);
		Seed.SetSeaLevel( (int)(WorldH*0.5f) );
		Seed.SetColor(R, G, B);
		
		// generate the peaks for this seed
		int peakspace = (int)(Math.random()*64) + 64;
		for (int i=0; i<WorldW/peakspace; i++)
		{
			int x = 768 + i*peakspace + (int)(Math.random()*8);
			int s = (int)(Math.random()*3);
			int w = (int)(Math.random()*256) + peakspace + 128;
			int h = (int)(Math.random() * w * 0.4);
			h *= (int)(Math.random()*2) * 2 - 1; // 50/50 invert height
			
			Seed.AddPeak(x, s, w, h);
		}
	}
	
	public static TerrainSeed GenerateSeed(int WorldW, int WorldH, int R, int G, int B)
	{
		// construct a new seed and generate some data for it
		TerrainSeed seed = new TerrainSeed();
		GenerateData(seed, WorldW, WorldH, R, G, B);
		
		// return the created seed
		return seed;
	}
	
	public static TerrainSeed GenerateSeed(int WorldW, int WorldH, int Shade)
	{
		// construct a new seed and generate some data for it
		TerrainSeed seed = new TerrainSeed();
		GenerateData(seed, WorldW, WorldH, Shade, Shade, Shade);
		
		// return the created seed
		return seed;
	}
}
