package io.github.cadiboo.optifinedeobf;

import io.github.cadiboo.optifinedeobf.mapping.SRG2MCP;
import io.github.cadiboo.optifinedeobf.mapping.TSRG2MCP;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Cadiboo
 */
public class MappingServiceTest {

	@Test
	public void srgShouldBeParsedProperly() {
		var input = """
			CL: net/minecraft/client/renderer/Quaternion net/minecraft/client/renderer/Quaternion
			CL: net/minecraft/client/renderer/Vector3f net/minecraft/client/renderer/Vector3f
			FD: net/minecraft/client/renderer/Quaternion/field_195895_a net/minecraft/client/renderer/Quaternion/components
			FD: net/minecraft/client/renderer/Vector3f/field_195907_a net/minecraft/client/renderer/Vector3f/components
			MD: net/minecraft/client/renderer/Vector3f/func_195906_d ()V net/minecraft/client/renderer/Vector3f/normalize ()V
			MD: net/minecraft/client/renderer/Vector3f/func_195900_b ()F net/minecraft/client/renderer/Vector3f/getY ()F
			""";
		var mapper = new SRG2MCP(new ByteArrayInputStream(input.getBytes()));
		assertEquals("components", mapper.mapField("net/minecraft/client/renderer/Quaternion", "field_195895_a"));
		assertEquals("normalize", mapper.mapMethod("net/minecraft/client/renderer/Vector3f", "func_195906_d", "()F"));
	}

	@Test
	public void tsrg1ShouldBeParsedProperly() {
		var input = """
			net/minecraft/client/renderer/Quaternion net/minecraft/client/renderer/Quaternion
			\tfield_195895_a components
			\tequals (Ljava/lang/Object;)Z equals
			\tfunc_195891_b ()F getY
			\tfunc_195892_e ()V conjugate
			net/minecraft/client/renderer/Vector3f net/minecraft/client/renderer/Vector3f
			\tfield_195907_a components
			\tequals (Ljava/lang/Object;)Z equals
			\tfunc_195896_c (Lnet/minecraft/client/renderer/Vector3f;)V cross
			\tfunc_195900_b ()F getY
			\tfunc_214906_c (FFF)F func_214906_c
			\thashCode ()I hashCode
			""";
		var mapper = new TSRG2MCP(new ByteArrayInputStream(input.getBytes()));
		assertEquals("components", mapper.mapField("net/minecraft/client/renderer/Quaternion", "field_195895_a"));
		assertEquals("cross", mapper.mapMethod("net/minecraft/client/renderer/Vector3f", "func_195896_c", "(Lnet/minecraft/client/renderer/Vector3f;)V"));
	}

	@Test
	public void tsrg2ShouldBeParsedProperly() {
		var input = """
		tsrg2 left right
		com/mojang/blaze3d/Blaze3D com/mojang/blaze3d/Blaze3D
			<init> ()V <init>
			m_166118_ (Lcom/mojang/blaze3d/pipeline/RenderPipeline;F)V process
				static
				0 p_166119_ p_166119_
				1 p_166120_ p_166120_
			m_166121_ (Lcom/mojang/blaze3d/pipeline/RenderPipeline;F)V render
				static
				0 p_166122_ p_166122_
				1 p_166123_ p_166123_
			m_83639_ ()V youJustLostTheGame
				static
			m_83640_ ()D getTime
				static
		com/mojang/blaze3d/DontObfuscate com/mojang/blaze3d/DontObfuscate
		com/mojang/blaze3d/FieldsAreNonnullByDefault com/mojang/blaze3d/FieldsAreNonnullByDefault
		com/mojang/blaze3d/MethodsReturnNonnullByDefault com/mojang/blaze3d/MethodsReturnNonnullByDefault
		com/mojang/blaze3d/audio/Channel com/mojang/blaze3d/audio/Channel
			f_166124_ BUFFER_DURATION_SECONDS
			f_166125_ QUEUED_BUFFER_COUNT
			f_83641_ LOGGER
			f_83642_ source
			f_83643_ initialized
			f_83644_ streamingBufferSize
			f_83645_ stream
			<clinit> ()V <clinit>
				static
			<init> (I)V <init>
				0 p_83648_ p_83648_
			m_166126_ ()Z playing
			m_83649_ ()Lcom/mojang/blaze3d/audio/Channel; create
				static
			m_83650_ (F)V setPitch
				0 p_83651_ p_83651_
			m_83652_ (I)V pumpBuffers
				0 p_83653_ p_83653_
			m_83654_ (Lnet/minecraft/world/phys/Vec3;)V setSelfPosition
				0 p_83655_ p_83655_
			m_83656_ (Lcom/mojang/blaze3d/audio/SoundBuffer;)V attachStaticBuffer
				0 p_83657_ p_83657_
			m_83658_ (Lnet/minecraft/client/sounds/AudioStream;)V attachBufferStream
				0 p_83659_ p_83659_
			m_83660_ (Ljavax/sound/sampled/AudioFormat;I)I calculateBufferSize
				static
				0 p_83661_ p_83661_
				1 p_83662_ p_83662_
			m_83663_ (Z)V setLooping
				0 p_83664_ p_83664_
			m_83665_ ()V destroy
			m_83666_ (F)V setVolume
				0 p_83667_ p_83667_
			m_83668_ (I)V lambda$pumpBuffers$1
				0 p_83669_ p_83669_
			m_83670_ (Z)V setRelative
				0 p_83671_ p_83671_
			m_83672_ ()V play
			m_83673_ (F)V linearAttenuation
				0 p_83674_ p_83674_
			m_83675_ (I)V lambda$attachStaticBuffer$0
				0 p_83676_ p_83676_
			m_83677_ ()V pause
			m_83678_ ()V unpause
			m_83679_ ()V stop
			m_83680_ ()Z stopped
			m_83681_ ()V disableAttenuation
			m_83682_ ()V updateStream
			m_83683_ ()I getState
			m_83684_ ()I removeProcessedBuffers
		""";
		var mapper = new TSRG2MCP(new ByteArrayInputStream(input.getBytes()));
		assertEquals("BUFFER_DURATION_SECONDS", mapper.mapField("com/mojang/blaze3d/audio/Channel", "f_166124_"));
		assertEquals("process", mapper.mapMethod("com/mojang/blaze3d/Blaze3D", "m_166118_", "(Lcom/mojang/blaze3d/pipeline/RenderPipeline;F)V"));
	}

}
