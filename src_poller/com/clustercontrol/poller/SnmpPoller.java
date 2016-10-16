package com.clustercontrol.poller;

import java.util.List;

import com.clustercontrol.sharedtable.DataTable;


public interface SnmpPoller {

	/**
	 * メインルーチン
	 * IPアドレスと　DataTableを受け取り、
	 * ポーリングした結果をDataTableに代入する
	 *
	 * @param ipAddress IPアドレス
	 * @param port ポート番号
	 * @param version バージョン（0:SNMP V1 protocol, 1:SNMP V2 protocol, 3: SNMP V3 protocol）
	 * @param community コミュニティ
	 * @param retries １回のポーリングでのリトライ回数
	 * @param timeout ポーリングのタイムアウト
	 * @param oidList 対象OIDのリスト
	 * @param securityLevel セキュリティレベル（v3）
	 * @param user ユーザ名（v3）
	 * @param authPassword 認証パスワード（v3）
	 * @param privPassword 暗号化パスワード（v3）
	 * @param authProtocol 認証プロトコル（v3）
	 * @param privProtocol 暗号化プロトコル（v3）
	 */
	public DataTable polling(
			String ipAddress,
			int port,
			int version,
			String community,
			int retries,
			int timeout,
			List<String> oidList,
			String securityLevel,
			String user,
			String authPassword,
			String privPassword,
			String authProtocol,
			String privProtocol);
}