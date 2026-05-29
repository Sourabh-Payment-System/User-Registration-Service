package payment.system.app.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import payment.system.app.dto.CreateWalletRequest;
import payment.system.app.dto.WalletResponse;
import payment.system.app.exception.WalletCreationException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletFacadeService {

    private final RestClient restClient;

    @Value("${wallet.service.base-url}")
    private String walletServiceBaseUrl;

    public WalletResponse createWallet(
            Long userId) {

        String url =
                walletServiceBaseUrl + "/wallets/create";

        log.info(
                "Calling wallet-service for userId={}",
                userId);

        try {

            WalletResponse response =
                    restClient.post()
                            .uri(url)
                            .body(
                                    new CreateWalletRequest(
                                            userId))
                            .retrieve()
                            .onStatus(
                                    HttpStatusCode::isError,
                                    (req, res) -> {

                                        throw new WalletCreationException(
                                                "Wallet service returned error status");
                                    })
                            .body(WalletResponse.class);

            if (response == null) {

                log.error(
                        "Wallet service returned null response for userId={}",
                        userId);

                throw new WalletCreationException(
                        "Wallet response is null");
            }

            log.info(
                    "Wallet created successfully for userId={}",
                    userId);

            return response;

        } catch (RestClientResponseException ex) {

            log.error(
                    "Wallet service HTTP error for userId={}, status={}, response={}",
                    userId,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);

            throw new WalletCreationException(
                    "Wallet service request failed",
                    ex);

        } catch (Exception ex) {

            log.error(
                    "Unexpected error while creating wallet for userId={}",
                    userId,
                    ex);

            throw new WalletCreationException(
                    "Wallet service unavailable",
                    ex);
        }
    }
}