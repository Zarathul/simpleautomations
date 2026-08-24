package net.zarathul.simpleautomations.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class AlcoholFluidEvaporationParticle extends SingleQuadParticle
{
	private AlcoholFluidEvaporationParticle(final ClientLevel level, final double x, final double y, final double z, final double xd, final double yd, final double zd, final TextureAtlasSprite sprite)
	{
		super(level, x, y, z, sprite);

		this.xd = xd;
		this.yd = yd;
		this.zd = zd;

		this.setSize(0.01f, 0.01f);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6f + 0.6f);
		this.lifetime = (int)(8.0f / (this.random.nextFloat() * 0.8 + 0.2));
		this.hasPhysics = false;
		this.friction = 1.0f;
		this.gravity = 0.0f;
	}

	@Override
	public void tick()
	{
		super.tick();
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.OPAQUE;
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet sprite;
		private final float[][] colors;

		public Provider(final SpriteSet sprite, int normalColor, int concentratedColor, int pureColor)
		{
			this.sprite = sprite;

			colors = new float[3][3];
			colors[0][0] = ARGB.redFloat(normalColor);
			colors[0][1] = ARGB.greenFloat(normalColor);
			colors[0][2] = ARGB.blueFloat(normalColor);
			colors[1][0] = ARGB.redFloat(concentratedColor);
			colors[1][1] = ARGB.greenFloat(concentratedColor);
			colors[1][2] = ARGB.blueFloat(concentratedColor);
			colors[2][0] = ARGB.redFloat(pureColor);
			colors[2][1] = ARGB.greenFloat(pureColor);
			colors[2][2] = ARGB.blueFloat(pureColor);
		}

		public Particle createParticle(
			final SimpleParticleType options,
			final ClientLevel level,
			final double x,
			final double y,
			final double z,
			final double xAux,
			final double yAux,
			final double zAux,
			final RandomSource random
		)
		{
			double xa = random.nextGaussian() * 0.5E-2f;
			double ya = 0.02 + random.nextGaussian() * 0.2E-2f;
			double za = random.nextGaussian() * 0.5E-2f;
			AlcoholFluidEvaporationParticle particle = new AlcoholFluidEvaporationParticle(level, x, y, z, xa, ya, za, this.sprite.get(random));

			int randomColorIndex = random.nextInt(3);
			particle.setColor(colors[randomColorIndex][0], colors[randomColorIndex][1], colors[randomColorIndex][2]);

			return particle;
		}
	}
}