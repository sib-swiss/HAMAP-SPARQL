package swiss.sib.hamap.sparql.jena;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

import junit.framework.TestCase;

public class PositionalFeatureShifterTest extends TestCase {

	/**
	 * @see https://www.icloud.com/keynote/000HrRbSyrsU-mifUN65HVHvQ#unirules%5Fgist%5Fand%5Ftricks
	 */
	@Test
	public void testTranslatingOneToTen() {
		final PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
		final List<NodeValue> args = Arrays.asList(NodeValue.makeString("TEVEI-DG--------VLH"),
				NodeValue.makeInteger(1), NodeValue.makeInteger(6), NodeValue.makeString("TiSVTIRE---------IKH"),
				NodeValue.makeInteger(3));
		final NodeValue exec = positionalFeatureShifter.exec(args);
		assertEquals(10, exec.getInteger().intValue());
	}

	@Test
	public void testTranslatingOneToTen2() {
		final PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
		final List<NodeValue> args = Arrays.asList(NodeValue.makeString("TEVEI-DG--------VLH"),
				NodeValue.makeInteger(1), NodeValue.makeInteger(6), NodeValue.makeString("TiSVTIRE---------IKH"),
				NodeValue.makeInteger(1));
		final NodeValue exec = positionalFeatureShifter.exec(args);
		assertEquals(8, exec.getInteger().intValue());
	}

	@Test
	public void testTranslatingExampleTwo() {
		final PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
		final List<NodeValue> args = Arrays.asList(NodeValue.makeString("TEVEI-DG--------VLH"),
				NodeValue.makeInteger(1), NodeValue.makeInteger(7), NodeValue.makeString("TiSVTIRE---------IKH"),
				NodeValue.makeInteger(3));
		final NodeValue exec = positionalFeatureShifter.exec(args);
		assertEquals(NodeValue.makeInteger(10), exec);
	}

////up_to_pos: aln string up to mapped position... e.g. aln "MGksNSK--LK" pos 3 on motif ('N' after s) will give: up_to_pos = "MGksN", after = "SK--LK", n_on_motif = 3
	@Test
	public void testTranslatingExampleFromScalaCode() {
		final PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
		final List<NodeValue> args = Arrays.asList(NodeValue.makeString("-N-TKLKPevVDDLT-KTYF"),
				NodeValue.makeInteger(1), NodeValue.makeInteger(8), NodeValue.makeString("-N-TKLKPevVDDLT"),
				NodeValue.makeInteger(1));
		final NodeValue exec = positionalFeatureShifter.exec(args);
		assertEquals(NodeValue.makeInteger(6), exec);
	}

	@Test
	public void testTranslatingNotEnoughArguments() {
		try {
			final PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
			final List<NodeValue> args = Arrays.asList(NodeValue.makeString("TEVEI-DG--------VLH"),
					NodeValue.makeInteger(1), NodeValue.makeInteger(7), NodeValue.makeString("TiSVTIRE---------IKH"));
			positionalFeatureShifter.exec(args);
			fail();
		} catch (ExprEvalException e) {
			assertTrue(true);
		}
	}
//" | 1                                   | "IDCRYYQQNECRSCQWLEIPYSQQLAEKQHHLKQQLISINCDK-AQWLAPFQSNEQCFRNKAKMLVSGSVERPILGILKNPNDPQSAIDLCDCPLYPARFSIIFSILKDFIGRAGLVPYNIAKQKGELKYILLTESIATGKLMLRFVLRTENKLPLIRRELPKLLEKLPHLEVVSVNLQPLHAAILEGEQEIFLTEQQFLPENFNSIPLFIRPQGFFQTNPKVAEGLYATAQQWVSELPIYNLWDLFCGVGGFGLHCAKALQEKWGKPIKLTGIEISSSAILAASHSAKILGLEHVNFQSLDAASVIEN--KNENKPDLVIVNPPRRGIGKELSEFLNQIQPHFILYSSCNAMTMGKDLQHLTCYKPLKIQLFDMFPQTSHYEVLVLLERK" | 1

	@Test
	public void testTranslatingFromDebugging() {
		PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
		NodeValue templateSigar = NodeValue.makeString(
				"MQCALYDAGRCRSCQWIMQPIPEQLSAKTADLKNLLADFP---VEEWCAPVSGPEQGFRNKAKMVVSGSVEKPLLGMLHRD---GTPEDLCDCPLYPASFAPVFAALKPFIARAGLTPYNVARKRGELKYILLTESQSDGGMMLRFVLRSDTKLAQLRKALPWLHEQLPQLKVITVNIQPVHMAIMEGETEIYLTEQQALAERFNDVPLWIRPQSFFQTNPAVASQLYATARDWVRQLPVKHMWDLFCGVGGFGLHCA-------TPDMQLTGIEIASEAIACAKQSAAELGLTRLQFQALDSTQFATA---QGDVPELVLVNPPRRGIGKPLCDYLSTMAPRFIIYSSCNAQTMAKDIRELPGFRIERVQLFDMFPHTAHYEVLTLLVKQ");
		List<NodeValue> args = Arrays.asList(templateSigar, NodeValue.makeInteger(1), NodeValue.makeInteger(87),
				NodeValue.makeString(
						"IDCRYYQQNECRSCQWLEIPYSQQLAEKQHHLKQQLISINCDK-AQWLAPFQSNEQCFRNKAKMLVSGSVERPILGILKNPNDPQSAIDLCDCPLYPARFSIIFSILKDFIGRAGLVPYNIAKQKGELKYILLTESIATGKLMLRFVLRTENKLPLIRRELPKLLEKLPHLEVVSVNLQPLHAAILEGEQEIFLTEQQFLPENFNSIPLFIRPQGFFQTNPKVAEGLYATAQQWVSELPIYNLWDLFCGVGGFGLHCAKALQEKWGKPIKLTGIEISSSAILAASHSAKILGLEHVNFQSLDAASVIEN--KNENKPDLVIVNPPRRGIGKELSEFLNQIQPHFILYSSCNAMTMGKDLQHLTCYKPLKIQLFDMFPQTSHYEVLVLLERK"),
				NodeValue.makeInteger(2));
		NodeValue exec = positionalFeatureShifter.exec(args);
		assertEquals(NodeValue.makeInteger(93), exec);
	}

	@Test
	public void testTranslatingFromDebugging2() {
		PositionalFeatureShifter positionalFeatureShifter = new PositionalFeatureShifter();
//DNKLQ VEAIK 10
//RGTVI DHIPA 20
//QIGFK LLSLF 30
//KLTET DQRIT 40
//IGLNL PSGEM 50
//GRKDL IKIEN 60
//TFLSE DQVDQ 70
//LALYA PQATV 80
//NRIDN YEVVG 90
//KSRPS LPERI 100
//DNVLV CPNSN 110
//CISHA -EPVS 120
//SSFAV RKRAN 130
//dIALK CKYCE 140
//KEFSH NVVLA 150		
		final NodeValue templateCigar = NodeValue.makeString(
				"DNKLQVEAIKRGTVIDHIPAQIGFKLLSLFKLTETDQRITIGLNLPSGEMGRKDLIKIENTFLSEDQVDQLALYAPQATVNRIDNYEVVGKSRPSLPERIDNVLVCPNSNCISHA-EPVSSSFAVRKRANdIALKCKYCEKEFSHNVVLA");
// from MF_00002.msa
//		                                                      DNKLQVEAIKRGTVIDHIPAQIGFKLLSLFKLTETDQR....ITIGLNLPSGE..M..GRKDLIKIENTFLSEDQVDQLALYAPQATVNRIDNYEVVGKSRPSLPERIDNVLVCPNSNCISHA-................EPVSSSFAVRKRANd..........IALKCKYCEKEFS..HNVVLA
//KNQLQ VEAIR 10
//HGSVI DHVPA 20
//GQGIK ILKLF 30
//QLIET QERIT 40
//VGFNL KSGAL 50
//GKKDL IKIEN 60
//TRLTE QQANQ 70
//LALFA PKATV 80
//NIIED FAVVK 90
//KHQLE LPEFI 100
//AGVFH CPNSN 110
//CISHN -EPVD 120
//SYFRV REVKG 130
//vVRMK CKYCE 140
//KSFT

		final NodeValue target__Cigar = NodeValue.makeString(
				"KNQLQVEAIRHGSVIDHVPAGQGIKILKLFQLIETQERITVGFNLKSGALGKKDLIKIENTRLTEQQANQLALFAPKATVNIIEDFAVVKKHQLELPEFIAGVFHCPNSNCISHN-EPVDSYFRVREVKGvVRMKCKYCEKSFT");
// from MF_00002.msa
//	KNQLQVEAIRHGSVIDHVPAGQGIKILKLFQLIETQER....ITVGFNLKSGA..L..GKKDLIKIENTRLTEQQANQLALFAPKATVNIIEDFAVVKKHQLELPEFIAGVFHCPNSNCISHN-................EPVDSYFRVREVKGv..........VRMKCKYCEKSFT..QDIVSE

		final NodeValue motifTemplateStart = NodeValue.makeInteger(4);

		final NodeValue motifTargetStart = NodeValue.makeInteger(4);

		{
			final NodeValue templatePositionToMap1 = NodeValue.makeInteger(109);

			NodeValue exec1 = positionalFeatureShifter.exec(templateCigar, motifTemplateStart, templatePositionToMap1,
					target__Cigar, motifTargetStart);
			assertEquals(templatePositionToMap1, exec1);

		}
		{
			final NodeValue templatePositionToMap2 = NodeValue.makeInteger(114);
			List<NodeValue> args2 = Arrays.asList(templateCigar, motifTemplateStart, templatePositionToMap2,
					target__Cigar, motifTargetStart);
			NodeValue exec2 = positionalFeatureShifter.exec(args2);
			assertEquals(templatePositionToMap2, exec2);
		}
		{
			final NodeValue templatePositionToMap3 = NodeValue.makeInteger(138);
			List<NodeValue> args3 = Arrays.asList(templateCigar, motifTemplateStart, templatePositionToMap3,
					target__Cigar, motifTargetStart);
			NodeValue exec3 = positionalFeatureShifter.exec(args3);
			assertEquals(templatePositionToMap3, exec3);
		}
		{
			final NodeValue templatePositionToMap4 = NodeValue.makeInteger(141);
			List<NodeValue> args4 = Arrays.asList(templateCigar, motifTemplateStart, templatePositionToMap4,
					target__Cigar, motifTargetStart);
			NodeValue exec4 = positionalFeatureShifter.exec(args4);
			assertEquals(templatePositionToMap4, exec4);
		}
	}
}
