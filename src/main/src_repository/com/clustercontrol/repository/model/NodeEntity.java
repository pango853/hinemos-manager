package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.ValidConstant;
import com.clustercontrol.commons.util.CryptUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;



/**
 * The persistent class for the cc_cfg_node database table.
 *
 */
@Entity
@Table(name="cc_cfg_node", schema="setting")
@Cacheable(true)
public class NodeEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private Integer autoDeviceSearch		= ValidConstant.TYPE_VALID;
	private String administrator			= "";
	private String characterSet				= "";
	private String cloudService				= "";
	private String cloudScope				= "";
	private String cloudResourceType		= "";
	private String cloudResourceId			= "";
	private String cloudResourceName		= "";
	private String cloudLocation			= "";
	private String contact					= "";
	private String hardwareType				= "";
	private String ipAddressV4				= "";
	private String ipAddressV6				= "";
	private Integer ipAddressVersion		= -1;
	private String ipmiIpAddress			= "";
	private String ipmiLevel				= "";
	private Integer ipmiPort				= 0;
	private String ipmiProtocol				= "RMCP+";
	private Integer ipmiRetryCount			= 3;
	private Integer ipmiTimeout				= 5000;
	private String ipmiUser					= "root";
	private String ipmiUserPassword			= "";
	private Integer jobPriority				= 16;
	private Integer jobMultiplicity			= 0;
	private String nodeName					= "";
	private String osName					= "";
	private String osRelease				= "";
	private String osVersion				= "";
	private String platformFamily			= "";
	private String snmpCommunity			= "public";
	private Integer snmpPort				= 161;
	private Integer snmpRetryCount			= 3;
	private Integer snmpTimeout				= 5000;
	private String snmpVersion				= "2c";
	private String snmpSecurityLevel			= SnmpSecurityLevelConstant.NOAUTH_NOPRIV;
	private String snmpUser					= "";
	private String snmpAuthPassword			= "";
	private String snmpPrivPassword			= "";
	private String snmpAuthProtocol			= SnmpProtocolConstant.MD5;
	private String snmpPrivProtocol			= SnmpProtocolConstant.DES;
	private String sshUser 					= "root";
	private String sshUserPassword				= "";
	private String sshPrivateKeyFilepath		= "";
	private String sshPrivateKeyPassphrase		= "";
	private Integer sshPort					= 22;
	private Integer sshTimeout					= 50000;
	private String subPlatformFamily		= "";
	private Integer wbemPort				= 5988;
	private String wbemProtocol				= "http";
	private Integer wbemRetryCount			= 3;
	private Integer wbemTimeout				= 5000;
	private String wbemUser					= "root";
	private String wbemUserPassword			= "";
	private Integer winrmPort				= 5985;
	private String winrmProtocol			= "http";
	private Integer winrmRetryCount			= 3;
	private Integer winrmTimeout			= 5000;
	private String winrmUser				= "Administrator";
	private String winrmUserPassword		= "";
	private String winrmVersion				= "";
	private Integer agentAwakePort			= 24005;
	private List<NodeCpuEntity> nodeCpuEntities;
	private List<NodeDeviceEntity> nodeDeviceEntities;
	private List<NodeDiskEntity> nodeDiskEntities;
	private FacilityEntity facilityEntity;
	private List<NodeFilesystemEntity> nodeFilesystemEntities;
	private List<NodeHostnameEntity> nodeHostnameEntities;
	private List<NodeMemoryEntity> nodeMemoryEntities;
	private List<NodeNetworkInterfaceEntity> nodeNetworkInterfaceEntities;
	private List<NodeNoteEntity> nodeNoteEntities;
	private List<NodeVariableEntity> nodeVariableEntities;

	@Deprecated
	public NodeEntity() {
	}

	public NodeEntity(FacilityEntity facilityEntity) {
		this.setFacilityId(facilityEntity.getFacilityId());
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		em.persist(this);
		this.relateToFacilityEntity(facilityEntity);
	}


	@Id
	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	public String getAdministrator() {
		return this.administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}


	@Column(name="auto_device_search")
	public Integer getAutoDeviceSearch() {
		return this.autoDeviceSearch;
	}

	public void setAutoDeviceSearch(Integer autoDeviceSearch) {
		this.autoDeviceSearch = autoDeviceSearch;
	}

	@Column(name="character_set")
	public String getCharacterSet() {
		return this.characterSet;
	}

	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}


	@Column(name="cloud_service")
	public String getCloudService() {
		return this.cloudService;
	}

	public void setCloudService(String cloudService) {
		this.cloudService = cloudService;
	}


	@Column(name="cloud_scope")
	public String getCloudScope() {
		return this.cloudScope;
	}

	public void setCloudScope(String cloudScope) {
		this.cloudScope = cloudScope;
	}


	@Column(name="cloud_resource_type")
	public String getCloudResourceType() {
		return this.cloudResourceType;
	}

	public void setCloudResourceType(String cloudResourceType) {
		this.cloudResourceType = cloudResourceType;
	}


	@Column(name="cloud_resource_id")
	public String getCloudResourceId() {
		return this.cloudResourceId;
	}

	public void setCloudResourceId(String cloudResourceId) {
		this.cloudResourceId = cloudResourceId;
	}
	
	@Column(name="cloud_resource_name")
	public String getCloudResourceName() {
		return this.cloudResourceName;
	}
	
	public void setCloudResourceName(String cloudResourceName) {
		this.cloudResourceName = cloudResourceName;
	}


	@Column(name="cloud_location")
	public String getCloudLocation() {
		return this.cloudLocation;
	}

	public void setCloudLocation(String cloudLocation) {
		this.cloudLocation = cloudLocation;
	}


	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}


	@Column(name="hardware_type")
	public String getHardwareType() {
		return this.hardwareType;
	}

	public void setHardwareType(String hardwareType) {
		this.hardwareType = hardwareType;
	}

	@Column(name="ip_address_v4")
	public String getIpAddressV4() {
		return this.ipAddressV4;
	}

	public void setIpAddressV4(String ipAddressV4) {
		this.ipAddressV4 = ipAddressV4;
	}


	@Column(name="ip_address_v6")
	public String getIpAddressV6() {
		return this.ipAddressV6;
	}

	public void setIpAddressV6(String ipAddressV6) {
		this.ipAddressV6 = ipAddressV6;
	}


	@Column(name="ip_address_version")
	public Integer getIpAddressVersion() {
		return this.ipAddressVersion;
	}

	public void setIpAddressVersion(Integer ipAddressVersion) {
		this.ipAddressVersion = ipAddressVersion;
	}


	@Column(name="ipmi_ip_address")
	public String getIpmiIpAddress() {
		return this.ipmiIpAddress;
	}

	public void setIpmiIpAddress(String ipmiIpAddress) {
		this.ipmiIpAddress = ipmiIpAddress;
	}


	@Column(name="ipmi_level")
	public String getIpmiLevel() {
		return this.ipmiLevel;
	}

	public void setIpmiLevel(String ipmiLevel) {
		this.ipmiLevel = ipmiLevel;
	}


	@Column(name="ipmi_port")
	public Integer getIpmiPort() {
		return this.ipmiPort;
	}

	public void setIpmiPort(Integer ipmiPort) {
		this.ipmiPort = ipmiPort;
	}


	@Column(name="ipmi_protocol")
	public String getIpmiProtocol() {
		return this.ipmiProtocol;
	}

	public void setIpmiProtocol(String ipmiProtocol) {
		this.ipmiProtocol = ipmiProtocol;
	}


	@Column(name="ipmi_retry_count")
	public Integer getIpmiRetryCount() {
		return this.ipmiRetryCount;
	}

	public void setIpmiRetryCount(Integer ipmiRetryCount) {
		this.ipmiRetryCount = ipmiRetryCount;
	}


	@Column(name="ipmi_timeout")
	public Integer getIpmiTimeout() {
		return this.ipmiTimeout;
	}

	public void setIpmiTimeout(Integer ipmiTimeout) {
		this.ipmiTimeout = ipmiTimeout;
	}


	@Column(name="job_priority")
	public Integer getJobPriority() {
		return this.jobPriority;
	}

	public void setJobPriority(Integer jobPriority) {
		this.jobPriority = jobPriority;
	}


	@Column(name="job_multiplicity")
	public Integer getJobMultiplicity() {
		return this.jobMultiplicity;
	}

	public void setJobMultiplicity(Integer jobMultiplicity) {
		this.jobMultiplicity = jobMultiplicity;
	}


	@Column(name="ipmi_user")
	public String getIpmiUser() {
		return this.ipmiUser;
	}

	public void setIpmiUser(String ipmiUser) {
		this.ipmiUser = ipmiUser;
	}

	@Transient
	public String getIpmiUserPassword() {
		return CryptUtil.decrypt(getIpmiUserPasswordCrypt());
	}

	public void setIpmiUserPassword(String ipmiUserPassword) {
		setIpmiUserPasswordCrypt(CryptUtil.encrypt(ipmiUserPassword));
	}

	
	@Column(name="ipmi_user_password")
	public String getIpmiUserPasswordCrypt() {
		return this.ipmiUserPassword;
	}

	public void setIpmiUserPasswordCrypt(String ipmiUserPassword) {
		this.ipmiUserPassword = ipmiUserPassword;
	}


	@Column(name="node_name")
	public String getNodeName() {
		return this.nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	@Column(name="os_name")
	public String getOsName() {
		return this.osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}


	@Column(name="os_release")
	public String getOsRelease() {
		return this.osRelease;
	}

	public void setOsRelease(String osRelease) {
		this.osRelease = osRelease;
	}


	@Column(name="os_version")
	public String getOsVersion() {
		return this.osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}


	@Column(name="platform_family")
	public String getPlatformFamily() {
		return this.platformFamily;
	}

	public void setPlatformFamily(String platformFamily) {
		this.platformFamily = platformFamily;
	}


	@Column(name="snmp_community")
	public String getSnmpCommunity() {
		return this.snmpCommunity;
	}

	public void setSnmpCommunity(String snmpCommunity) {
		this.snmpCommunity = snmpCommunity;
	}


	@Column(name="snmp_port")
	public Integer getSnmpPort() {
		return this.snmpPort;
	}

	public void setSnmpPort(Integer snmpPort) {
		this.snmpPort = snmpPort;
	}


	@Column(name="snmp_retry_count")
	public Integer getSnmpRetryCount() {
		return this.snmpRetryCount;
	}

	public void setSnmpRetryCount(Integer snmpRetryCount) {
		this.snmpRetryCount = snmpRetryCount;
	}


	@Column(name="snmp_timeout")
	public Integer getSnmpTimeout() {
		return this.snmpTimeout;
	}

	public void setSnmpTimeout(Integer snmpTimeout) {
		this.snmpTimeout = snmpTimeout;
	}


	@Column(name="snmp_version")
	public String getSnmpVersion() {
		return this.snmpVersion;
	}

	public void setSnmpVersion(String snmpVersion) {
		this.snmpVersion = snmpVersion;
	}


	@Column(name="snmp_security_level")
	public String getSnmpSecurityLevel() {
		return this.snmpSecurityLevel;
	}

	public void setSnmpSecurityLevel(String snmpSecurityLevel) {
		this.snmpSecurityLevel = snmpSecurityLevel;
	}


	@Column(name="snmp_user")
	public String getSnmpUser() {
		return this.snmpUser;
	}

	public void setSnmpUser(String snmpUser) {
		this.snmpUser = snmpUser;
	}

	@Transient
	public String getSnmpAuthPassword() {
		return CryptUtil.decrypt(getSnmpAuthPasswordCrypt());
	}

	public void setSnmpAuthPassword(String snmpAuthPassword) {
		setSnmpAuthPasswordCrypt(CryptUtil.encrypt(snmpAuthPassword));
	}

	@Column(name="snmp_auth_password")
	public String getSnmpAuthPasswordCrypt() {
		return this.snmpAuthPassword;
	}

	public void setSnmpAuthPasswordCrypt(String snmpAuthPassword) {
		this.snmpAuthPassword = snmpAuthPassword;
	}


	@Transient
	public String getSnmpPrivPassword() {
		return CryptUtil.decrypt(getSnmpPrivPasswordCrypt());
	}

	public void setSnmpPrivPassword(String snmpPrivPassword) {
		setSnmpPrivPasswordCrypt(CryptUtil.encrypt(snmpPrivPassword));
	}


	@Column(name="snmp_priv_password")
	public String getSnmpPrivPasswordCrypt() {
		return this.snmpPrivPassword;
	}

	public void setSnmpPrivPasswordCrypt(String snmpPrivPassword) {
		this.snmpPrivPassword = snmpPrivPassword;
	}


	@Column(name="snmp_auth_protocol")
	public String getSnmpAuthProtocol() {
		return this.snmpAuthProtocol;
	}

	public void setSnmpAuthProtocol(String snmpAuthProtocol) {
		this.snmpAuthProtocol = snmpAuthProtocol;
	}


	@Column(name="snmp_priv_protocol")
	public String getSnmpPrivProtocol() {
		return this.snmpPrivProtocol;
	}

	public void setSnmpPrivProtocol(String snmpPrivProtocol) {
		this.snmpPrivProtocol = snmpPrivProtocol;
	}

	@Column(name="ssh_user")
	public String getSshUser() {
		return this.sshUser;
	}
	
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	
	@Transient
	public String getSshUserPassword() {
		return CryptUtil.decrypt(getSshUserPasswordCrypt());
	}
	
	public void setSshUserPassword(String sshUserPassword) {
		setSshUserPasswordCrypt(CryptUtil.encrypt(sshUserPassword));
	}

	@Column(name="ssh_user_password")
	public String getSshUserPasswordCrypt() {
		return this.sshUserPassword;
	}
	
	public void setSshUserPasswordCrypt(String sshUserPassword) {
		this.sshUserPassword = sshUserPassword;
	}
	
	@Column(name="ssh_private_key_filepath")
	public String getSshPrivateKeyFilepath() {
		return this.sshPrivateKeyFilepath;
	}
	
	public void setSshPrivateKeyFilepath(String sshPrivateKeyFilepath) {
		this.sshPrivateKeyFilepath = sshPrivateKeyFilepath;
	}

	@Transient
	public String getSshPrivateKeyPassphrase() {
		return CryptUtil.decrypt(getSshPrivateKeyPassphraseCrypt());
	}
	
	public void setSshPrivateKeyPassphrase(String sshPrivateKeyPassphrase) {
		setSshPrivateKeyPassphraseCrypt(CryptUtil.encrypt(sshPrivateKeyPassphrase));
	}
	
	@Column(name="ssh_private_key_passphrase")
	public String getSshPrivateKeyPassphraseCrypt() {
		return this.sshPrivateKeyPassphrase;
	}
	
	public void setSshPrivateKeyPassphraseCrypt(String sshPrivateKeyPassphrase) {
		this.sshPrivateKeyPassphrase = sshPrivateKeyPassphrase;
	}
	
	@Column(name="ssh_port")
	public Integer getSshPort() {
		return this.sshPort;
	}
	
	public void setSshPort(Integer sshPort) {
		this.sshPort = sshPort;
	}
	
	@Column(name="ssh_timeout")
	public Integer getSshTimeout() {
		return this.sshTimeout;
	}
	
	public void setSshTimeout(Integer sshTimeout) {
		this.sshTimeout = sshTimeout;
	}

	@Column(name="sub_platform_family")
	public String getSubPlatformFamily() {
		return this.subPlatformFamily;
	}

	public void setSubPlatformFamily(String subPlatformFamily) {
		this.subPlatformFamily = subPlatformFamily;
	}

	@Column(name="wbem_port")
	public Integer getWbemPort() {
		return this.wbemPort;
	}

	public void setWbemPort(Integer wbemPort) {
		this.wbemPort = wbemPort;
	}


	@Column(name="wbem_protocol")
	public String getWbemProtocol() {
		return this.wbemProtocol;
	}

	public void setWbemProtocol(String wbemProtocol) {
		this.wbemProtocol = wbemProtocol;
	}


	@Column(name="wbem_retry_count")
	public Integer getWbemRetryCount() {
		return this.wbemRetryCount;
	}

	public void setWbemRetryCount(Integer wbemRetryCount) {
		this.wbemRetryCount = wbemRetryCount;
	}


	@Column(name="wbem_timeout")
	public Integer getWbemTimeout() {
		return this.wbemTimeout;
	}

	public void setWbemTimeout(Integer wbemTimeout) {
		this.wbemTimeout = wbemTimeout;
	}


	@Column(name="wbem_user")
	public String getWbemUser() {
		return this.wbemUser;
	}

	public void setWbemUser(String wbemUser) {
		this.wbemUser = wbemUser;
	}

	@Transient
	public String getWbemUserPassword() {
		return CryptUtil.decrypt(getWbemUserPasswordCrypt());
	}

	public void setWbemUserPassword(String wbemUserPassword) {
		setWbemUserPasswordCrypt(CryptUtil.encrypt(wbemUserPassword));
	}

	@Column(name="wbem_user_password")
	public String getWbemUserPasswordCrypt() {
		return this.wbemUserPassword;
	}

	public void setWbemUserPasswordCrypt(String wbemUserPassword) {
		this.wbemUserPassword = wbemUserPassword;
	}


	@Column(name="winrm_port")
	public Integer getWinrmPort() {
		return this.winrmPort;
	}

	public void setWinrmPort(Integer winrmPort) {
		this.winrmPort = winrmPort;
	}


	@Column(name="winrm_protocol")
	public String getWinrmProtocol() {
		return this.winrmProtocol;
	}

	public void setWinrmProtocol(String winrmProtocol) {
		this.winrmProtocol = winrmProtocol;
	}


	@Column(name="winrm_retry_count")
	public Integer getWinrmRetryCount() {
		return this.winrmRetryCount;
	}

	public void setWinrmRetryCount(Integer winrmRetryCount) {
		this.winrmRetryCount = winrmRetryCount;
	}


	@Column(name="winrm_timeout")
	public Integer getWinrmTimeout() {
		return this.winrmTimeout;
	}

	public void setWinrmTimeout(Integer winrmTimeout) {
		this.winrmTimeout = winrmTimeout;
	}


	@Column(name="winrm_user")
	public String getWinrmUser() {
		return this.winrmUser;
	}

	public void setWinrmUser(String winrmUser) {
		this.winrmUser = winrmUser;
	}

	@Transient
	public String getWinrmUserPassword() {
		return CryptUtil.decrypt(getWinrmUserPasswordCrypt());
	}

	public void setWinrmUserPassword(String winrmUserPassword) {
		setWinrmUserPasswordCrypt(CryptUtil.encrypt(winrmUserPassword));
	}

	@Column(name="winrm_user_password")
	public String getWinrmUserPasswordCrypt() {
		return this.winrmUserPassword;
	}

	public void setWinrmUserPasswordCrypt(String winrmUserPassword) {
		this.winrmUserPassword = winrmUserPassword;
	}


	@Column(name="winrm_version")
	public String getWinrmVersion() {
		return this.winrmVersion;
	}

	public void setWinrmVersion(String winrmVersion) {
		this.winrmVersion = winrmVersion;
	}

	@Column(name="agent_awake_port")
	public Integer getAgentAwakePort() {
		return this.agentAwakePort;
	}

	public void setAgentAwakePort(Integer agentAwakePort) {
		this.agentAwakePort = agentAwakePort;
	}

	//bi-directional many-to-one association to NodeCpuEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeCpuEntity> getNodeCpuEntities() {
		return this.nodeCpuEntities;
	}

	public void setNodeCpuEntities(List<NodeCpuEntity> nodeCpuEntities) {
		this.nodeCpuEntities = nodeCpuEntities;
	}


	//bi-directional many-to-one association to NodeDeviceEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeDeviceEntity> getNodeDeviceEntities() {
		return this.nodeDeviceEntities;
	}

	public void setNodeDeviceEntities(List<NodeDeviceEntity> nodeDeviceEntities) {
		this.nodeDeviceEntities = nodeDeviceEntities;
	}


	//bi-directional many-to-one association to NodeDiskEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeDiskEntity> getNodeDiskEntities() {
		return this.nodeDiskEntities;
	}

	public void setNodeDiskEntities(List<NodeDiskEntity> nodeDiskEntities) {
		this.nodeDiskEntities = nodeDiskEntities;
	}


	//bi-directional one-to-one association to FacilityEntity
	@OneToOne(fetch=FetchType.LAZY, optional=false)
	@PrimaryKeyJoinColumn(name="facility_id")
	public FacilityEntity getFacilityEntity() {
		return this.facilityEntity;
	}

	@Deprecated
	public void setFacilityEntity(FacilityEntity facilityEntity) {
		this.facilityEntity = facilityEntity;
	}

	/**
	 * NodeEntityオブジェクト参照設定<BR>
	 *
	 * NodeEntity設定時はSetterに代わりこちらを使用すること。
	 *
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 *
	 * JSR 220 3.2.3 Synchronization to the Database
	 *
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToFacilityEntity(FacilityEntity facilityEntity) {
		this.setFacilityEntity(facilityEntity);
		if (facilityEntity != null) {
			facilityEntity.setNodeEntity(this);
		}
	}


	//bi-directional many-to-one association to NodeFilesystemEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeFilesystemEntity> getNodeFilesystemEntities() {
		return this.nodeFilesystemEntities;
	}

	public void setNodeFilesystemEntities(List<NodeFilesystemEntity> nodeFilesystemEntities) {
		this.nodeFilesystemEntities = nodeFilesystemEntities;
	}


	//bi-directional many-to-one association to NodeHostnameEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeHostnameEntity> getNodeHostnameEntities() {
		return this.nodeHostnameEntities;
	}

	public void setNodeHostnameEntities(List<NodeHostnameEntity> nodeHostnameEntities) {
		this.nodeHostnameEntities = nodeHostnameEntities;
	}


	//bi-directional many-to-one association to NodeMemoryEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeMemoryEntity> getNodeMemoryEntities() {
		return this.nodeMemoryEntities;
	}

	public void setNodeMemoryEntities(List<NodeMemoryEntity> nodeMemoryEntities) {
		this.nodeMemoryEntities = nodeMemoryEntities;
	}


	//bi-directional many-to-one association to NodeNetworkInterfaceEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeNetworkInterfaceEntity> getNodeNetworkInterfaceEntities() {
		return this.nodeNetworkInterfaceEntities;
	}

	public void setNodeNetworkInterfaceEntities(List<NodeNetworkInterfaceEntity> nodeNetworkInterfaceEntities) {
		this.nodeNetworkInterfaceEntities = nodeNetworkInterfaceEntities;
	}


	//bi-directional many-to-one association to NodeNoteEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeNoteEntity> getNodeNoteEntities() {
		return this.nodeNoteEntities;
	}

	public void setNodeNoteEntities(List<NodeNoteEntity> nodeNoteEntities) {
		this.nodeNoteEntities = nodeNoteEntities;
	}


	//bi-directional many-to-one association to NodeVariableEntity
	@OneToMany(mappedBy="nodeEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<NodeVariableEntity> getNodeVariableEntities() {
		return this.nodeVariableEntities;
	}

	public void setNodeVariableEntities(List<NodeVariableEntity> nodeVariableEntities) {
		this.nodeVariableEntities = nodeVariableEntities;
	}

	/**
	 * 削除前処理<BR>
	 *
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 *
	 * JSR 220 3.2.3 Synchronization to the Database
	 *
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// FacilityEntity
		if (this.facilityEntity != null) {
			this.facilityEntity.setNodeEntity(null);
		}
	}
	/**
	 * NodeCpuEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeCpuEntities(List<NodeCpuEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeCpuEntity> list = this.getNodeCpuEntities();
		Iterator<NodeCpuEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeCpuEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeDeviceEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeDeviceEntities(List<NodeDeviceEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeDeviceEntity> list = this.getNodeDeviceEntities();
		Iterator<NodeDeviceEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeDeviceEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeDiskEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeDiskEntities(List<NodeDiskEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeDiskEntity> list = this.getNodeDiskEntities();
		Iterator<NodeDiskEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeDiskEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeFilesystemEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeFilesystemEntities(List<NodeFilesystemEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeFilesystemEntity> list = this.getNodeFilesystemEntities();
		Iterator<NodeFilesystemEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeFilesystemEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeHostnameEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeHostnameEntities(List<NodeHostnameEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeHostnameEntity> list = this.getNodeHostnameEntities();
		Iterator<NodeHostnameEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeHostnameEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeMemoryEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeMemoryEntities(List<NodeMemoryEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeMemoryEntity> list = this.getNodeMemoryEntities();
		Iterator<NodeMemoryEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeMemoryEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeNetworkInterfaceEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeNetworkInterfaceEntities(List<NodeNetworkInterfaceEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeNetworkInterfaceEntity> list = this.getNodeNetworkInterfaceEntities();
		Iterator<NodeNetworkInterfaceEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeNetworkInterfaceEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeNoteEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeNoteEntities(List<NodeNoteEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeNoteEntity> list = this.getNodeNoteEntities();
		Iterator<NodeNoteEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeNoteEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}

	/**
	 * NodeVariableEntity削除<BR>
	 *
	 * 指定されたPK以外の子Entityを削除する。
	 *
	 */
	public void deleteNodeVariableEntities(List<NodeVariableEntityPK> notDelPkList) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeVariableEntity> list = this.getNodeVariableEntities();
		Iterator<NodeVariableEntity> iter = list.iterator();
		while(iter.hasNext()) {
			NodeVariableEntity entity = iter.next();
			if (!notDelPkList.contains(entity.getId())) {
				iter.remove();
				em.remove(entity);
			}
		}
	}


}