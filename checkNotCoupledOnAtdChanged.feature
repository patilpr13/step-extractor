@suite-FCPM
@HLR-TFPL-00001
Feature: GMC/Departure/checkNotCoupledOnAtdChanged/FPL

  No test context needed.

  Background: Departed Flight Plan with ATD
    Given the Current Time is '2023-02-07T09:00:00Z'
    And the following 'ACTIVE' FPL has been created:
      | Callsign | Flight Rules | Aircraft Type (F9b) | Nav Equipment | Surv Equipment | ADEP (F13a) | EOBT  | PFL  | Route                              | ADES (F16a) | Other Information | Supplementary Information |
      | FPL001   | I            | A320                | N             | S              | YNEN        | 09:00 | F200 | ADKUL IDOTO IKUMA ADKUL SHEPP YPHU | YBES        | DOF/230207        | R/UV                      |
    And the Flight has been Departed with ATD '09:06:00'

  @HLT-TFPL-13796
  Scenario: Rejects the request when Flight Plan is High Rate Surveillance Coupled and ATD is removed
    Given the Flight has been High Rate Surveillance Coupled
    And the Flight is Low Rate Surveillance Uncoupled
    When ASX-FlightAttributesUpdate is requested with:
      | Callsign | Flight Rules | Nb Of Aircraft | Aircraft Type (F9b) | Nav Equipment | Surv Equipment | ADEP (F13a) | EOBDT                | PFL  | Route                              | ADES (F16a) | Other Information | Supplementary Information | ATD |
      | FPL001   | I            |              1 | A320                | N             | S              | YNEN        | 2023-02-07T09:00:00Z | F200 | ADKUL IDOTO IKUMA ADKUL SHEPP YPHU | YBES        | DOF/230207        | R/UV                      |     |
    Then the ASX request shall be rejected with reason 'Flight is already coupled'

  @HLT-TFPL-13797
  Scenario: Rejects the request when Flight Plan is Low Rate Surveillance Coupled and ATD is removed
    Given the Flight has been Low Rate Surveillance Coupled
    And the Flight is High Rate Surveillance Uncoupled
    When ASX-FlightAttributesUpdate is requested with:
      | Callsign | Flight Rules | Nb Of Aircraft | Aircraft Type (F9b) | Nav Equipment | Surv Equipment | ADEP (F13a) | EOBDT                | PFL  | Route                              | ADES (F16a) | Other Information | Supplementary Information | ATD |
      | FPL001   | I            |              1 | A320                | N             | S              | YNEN        | 2023-02-07T09:00:00Z | F200 | ADKUL IDOTO IKUMA ADKUL SHEPP YPHU | YBES        | DOF/230207        | R/UV                      |     |
    Then the ASX request shall be rejected with reason 'Flight is already coupled'
