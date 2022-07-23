package com.example.localstack;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
class LocalstackApplicationTests {
	private static final DockerImageName localstackImage =
			DockerImageName.parse("localstack/localstack:0.11.3");
	@Container
	private static final LocalStackContainer localstack = new LocalStackContainer(localstackImage).withServices(S3);

	@Test
	void testLocalstackAndS3() {
		assertTrue(localstack.isRunning());

		AmazonS3 s3 = AmazonS3ClientBuilder
				.standard()
				.withEndpointConfiguration(
						new AwsClientBuilder.EndpointConfiguration(
								localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
								localstack.getRegion()
						)
				)
				.withCredentials(
						new AWSStaticCredentialsProvider(
								new BasicAWSCredentials(localstack.getAccessKey(), localstack.getSecretKey())
						)
				)
				.build();

		Bucket b = s3.createBucket("test");
		assertTrue(s3.doesBucketExistV2(b.getName()));

		s3.deleteBucket(b.getName());

		assertFalse(s3.doesBucketExistV2(b.getName()));
	}
}
